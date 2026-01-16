import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { AuthService } from './auth.service';
import { ErrorHandlerService } from './error-handler.service';

// ===================== Interfaces =====================

export interface ChatConversation {
  id: number;
  studentId: number;
  studentName: string;
  instructorId: number;
  instructorName: string;
  internshipId: number;
  internshipTitle: string;
  lastMessage?: string;
  lastMessageTime?: string;
  lastMessageSenderId?: number;
  unreadCountStudent: number;
  unreadCountInstructor: number;
  createdAt: string;
  updatedAt: string;
  otherParticipantId?: number;
  otherParticipantName?: string;
  myUnreadCount?: number;
}

export interface ChatMessage {
  id: number;
  conversationId: number;
  senderId: number;
  senderName: string;
  content: string;
  type: 'TEXT' | 'FILE' | 'IMAGE' | 'SYSTEM';
  status: 'SENT' | 'DELIVERED' | 'READ';
  attachmentUrl?: string;
  attachmentName?: string;
  attachmentType?: string;
  attachmentSize?: number;
  replyToMessageId?: number;
  replyToContent?: string;
  sentAt: string;
  deliveredAt?: string;
  readAt?: string;
  deleted: boolean;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface SendMessageRequest {
  conversationId: number;
  content: string;
  messageType?: 'TEXT' | 'FILE' | 'IMAGE';
  fileName?: string;
  fileUrl?: string;
  fileSize?: number;
  fileType?: string;
  replyToId?: number;
}

export interface StartConversationRequest {
  otherUserId: number;
  internshipId: number;
  initialMessage?: string;
}

export interface TypingIndicator {
  conversationId: number;
  userId: number;
  userName: string;
}

// ===================== Service =====================

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = `${environment.apiUrl}/chat`;
  private wsUrl = environment.apiUrl.replace('/api', '/ws');
  
  private stompClient: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  
  // Observables for real-time updates
  private newMessageSubject = new Subject<ChatMessage>();
  private typingSubject = new Subject<TypingIndicator>();
  private messageReadSubject = new Subject<{ conversationId: number; userId: number }>();
  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  
  public newMessage$ = this.newMessageSubject.asObservable();
  public typing$ = this.typingSubject.asObservable();
  public messageRead$ = this.messageReadSubject.asObservable();
  public connectionStatus$ = this.connectionStatusSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private errorHandler: ErrorHandlerService
  ) {}

  // ===================== WebSocket Connection =====================

  connect(): void {
    const token = this.authService.getToken();
    if (!token) {
      console.error('No token available for WebSocket connection');
      this.errorHandler.handleWebSocketError(new Error('Authentication required for chat'));
      return;
    }

    try {
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS(`${this.wsUrl}?token=${token}`),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: (str) => {
          if (environment.production === false) {
            console.log('STOMP: ' + str);
          }
        }
      });

      this.stompClient.onConnect = () => {
        console.log('WebSocket Connected');
        this.connectionStatusSubject.next(true);
      };

      this.stompClient.onDisconnect = () => {
        console.log('WebSocket Disconnected');
        this.connectionStatusSubject.next(false);
      };

      this.stompClient.onStompError = (frame) => {
        console.error('STOMP Error:', frame.headers['message']);
        this.errorHandler.handleWebSocketError(new Error(frame.headers['message'] || 'Chat connection error'));
      };

      this.stompClient.onWebSocketError = (event) => {
        console.error('WebSocket Error:', event);
        this.errorHandler.handleWebSocketError(new Error('Chat connection failed. Retrying...'));
      };

      this.stompClient.activate();
    } catch (error) {
      console.error('Failed to initialize WebSocket:', error);
      this.errorHandler.handleWebSocketError(error as Error);
    }
  }

  disconnect(): void {
    if (this.stompClient) {
      this.subscriptions.forEach(sub => sub.unsubscribe());
      this.subscriptions.clear();
      this.stompClient.deactivate();
      this.stompClient = null;
      this.connectionStatusSubject.next(false);
    }
  }

  // ===================== WebSocket Subscriptions =====================

  subscribeToConversation(conversationId: number): void {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket not connected');
      return;
    }

    const subKey = `conversation-${conversationId}`;
    if (this.subscriptions.has(subKey)) {
      return; // Already subscribed
    }

    // Subscribe to new messages
    const messageSub = this.stompClient.subscribe(
      `/topic/conversation/${conversationId}/messages`,
      (message: IMessage) => {
        const chatMessage: ChatMessage = JSON.parse(message.body);
        this.newMessageSubject.next(chatMessage);
      }
    );

    // Subscribe to typing indicators
    const typingSub = this.stompClient.subscribe(
      `/topic/conversation/${conversationId}/typing`,
      (message: IMessage) => {
        const typing: TypingIndicator = JSON.parse(message.body);
        this.typingSubject.next(typing);
      }
    );

    // Subscribe to read receipts
    const readSub = this.stompClient.subscribe(
      `/topic/conversation/${conversationId}/read`,
      (message: IMessage) => {
        const readInfo = JSON.parse(message.body);
        this.messageReadSubject.next(readInfo);
      }
    );

    this.subscriptions.set(subKey, messageSub);
    this.subscriptions.set(`${subKey}-typing`, typingSub);
    this.subscriptions.set(`${subKey}-read`, readSub);
  }

  unsubscribeFromConversation(conversationId: number): void {
    const subKey = `conversation-${conversationId}`;
    
    [subKey, `${subKey}-typing`, `${subKey}-read`].forEach(key => {
      const sub = this.subscriptions.get(key);
      if (sub) {
        sub.unsubscribe();
        this.subscriptions.delete(key);
      }
    });
  }

  // ===================== WebSocket Send Methods =====================

  sendMessageWs(conversationId: number, content: string, replyToId?: number): void {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket not connected');
      return;
    }

    const request: SendMessageRequest = { conversationId, content, replyToId };
    this.stompClient.publish({
      destination: `/app/chat/${conversationId}/send`,
      body: JSON.stringify(request)
    });
  }

  sendTypingIndicator(conversationId: number): void {
    if (!this.stompClient || !this.stompClient.connected) {
      return;
    }

    this.stompClient.publish({
      destination: `/app/chat/${conversationId}/typing`,
      body: ''
    });
  }

  markAsReadWs(conversationId: number): void {
    if (!this.stompClient || !this.stompClient.connected) {
      return;
    }

    this.stompClient.publish({
      destination: `/app/chat/${conversationId}/read`,
      body: ''
    });
  }

  // ===================== REST API Methods =====================

  getConversations(): Observable<ChatConversation[]> {
    return this.http.get<ChatConversation[]>(`${this.apiUrl}/conversations`);
  }

  startConversation(request: StartConversationRequest): Observable<ChatConversation> {
    return this.http.post<ChatConversation>(`${this.apiUrl}/conversations/start`, request);
  }

  getConversation(conversationId: number): Observable<ChatConversation> {
    return this.http.get<ChatConversation>(`${this.apiUrl}/conversations/${conversationId}`);
  }

  getMessages(conversationId: number, page: number = 0, size: number = 50): Observable<Page<ChatMessage>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'sentAt,desc');
    
    return this.http.get<Page<ChatMessage>>(
      `${this.apiUrl}/conversations/${conversationId}/messages`,
      { params }
    );
  }

  sendMessage(request: SendMessageRequest): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.apiUrl}/messages`, request);
  }

  sendMessageWithAttachment(
    conversationId: number,
    file: File,
    content?: string,
    replyToMessageId?: number
  ): Observable<ChatMessage> {
    const formData = new FormData();
    formData.append('file', file);
    if (content) {
      formData.append('content', content);
    }
    if (replyToMessageId) {
      formData.append('replyToMessageId', replyToMessageId.toString());
    }
    
    return this.http.post<ChatMessage>(
      `${this.apiUrl}/conversations/${conversationId}/messages/attachment`,
      formData
    );
  }

  markAsRead(conversationId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/conversations/${conversationId}/read`, {});
  }

  deleteMessage(messageId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/messages/${messageId}`);
  }

  searchMessages(conversationId: number, query: string, page: number = 0): Observable<Page<ChatMessage>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', '20');
    
    return this.http.get<Page<ChatMessage>>(
      `${this.apiUrl}/conversations/${conversationId}/search`,
      { params }
    );
  }

  searchConversations(query: string): Observable<ChatConversation[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<ChatConversation[]>(`${this.apiUrl}/conversations/search`, { params });
  }

  getUnreadCount(): Observable<{ unreadCount: number }> {
    return this.http.get<{ unreadCount: number }>(`${this.apiUrl}/unread-count`);
  }
}
