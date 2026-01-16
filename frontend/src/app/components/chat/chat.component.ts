import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatListModule } from '@angular/material/list';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { Subject, Subscription, debounceTime, distinctUntilChanged } from 'rxjs';

import { ChatService, ChatConversation, ChatMessage, TypingIndicator } from '../../services/chat.service';
import { AuthService } from '../../services/auth.service';
import { SharedLayoutComponent } from '../shared-layout/shared-layout.component';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatListModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatTooltipModule,
    MatDividerModule,
    MatChipsModule,
    SharedLayoutComponent
  ],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  @ViewChild('messageInput') messageInput!: ElementRef;

  conversations: ChatConversation[] = [];
  selectedConversation: ChatConversation | null = null;
  messages: ChatMessage[] = [];
  
  newMessage = '';
  searchQuery = '';
  isLoadingConversations = false;
  isLoadingMessages = false;
  isSending = false;
  isConnected = false;
  
  currentUserId: number = 0;
  currentUserRole: string = '';
  
  typingUsers: Map<number, string> = new Map();
  typingTimeout: any;
  
  replyingTo: ChatMessage | null = null;
  
  private subscriptions: Subscription[] = [];
  private typingSubject = new Subject<void>();
  private shouldScrollToBottom = false;

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initializeUser();
    this.loadConversations();
    this.setupWebSocket();
    this.setupTypingDebounce();
    
    // Check if conversation ID is in route
    this.route.params.subscribe(params => {
      if (params['conversationId']) {
        this.selectConversationById(+params['conversationId']);
      }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.chatService.disconnect();
    if (this.typingTimeout) {
      clearTimeout(this.typingTimeout);
    }
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private initializeUser(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      // Get userId from the stored user object
      this.currentUserId = user.userId || 0;
      this.currentUserRole = user.role;
      console.log('Chat initialized for user ID:', this.currentUserId);
    }
  }

  private setupWebSocket(): void {
    this.chatService.connect();
    
    // Connection status
    this.subscriptions.push(
      this.chatService.connectionStatus$.subscribe(connected => {
        this.isConnected = connected;
        if (connected && this.selectedConversation) {
          this.chatService.subscribeToConversation(this.selectedConversation.id);
        }
      })
    );
    
    // New messages
    this.subscriptions.push(
      this.chatService.newMessage$.subscribe(message => {
        if (this.selectedConversation && message.conversationId === this.selectedConversation.id) {
          this.messages.push(message);
          this.shouldScrollToBottom = true;
          
          // Mark as read if window is focused
          if (document.hasFocus()) {
            this.chatService.markAsRead(this.selectedConversation.id).subscribe();
          }
        }
        
        // Update conversation list
        this.updateConversationLastMessage(message);
      })
    );
    
    // Typing indicators
    this.subscriptions.push(
      this.chatService.typing$.subscribe(typing => {
        if (typing.userId !== this.currentUserId) {
          this.typingUsers.set(typing.userId, typing.userName);
          
          // Clear after 3 seconds
          setTimeout(() => {
            this.typingUsers.delete(typing.userId);
          }, 3000);
        }
      })
    );
    
    // Read receipts
    this.subscriptions.push(
      this.chatService.messageRead$.subscribe(({ conversationId, userId }) => {
        if (this.selectedConversation && conversationId === this.selectedConversation.id) {
          this.messages.forEach(msg => {
            if (msg.senderId === this.currentUserId && msg.status !== 'READ') {
              msg.status = 'READ';
            }
          });
        }
      })
    );
  }

  private setupTypingDebounce(): void {
    this.subscriptions.push(
      this.typingSubject.pipe(
        debounceTime(300),
        distinctUntilChanged()
      ).subscribe(() => {
        if (this.selectedConversation) {
          this.chatService.sendTypingIndicator(this.selectedConversation.id);
        }
      })
    );
  }

  loadConversations(): void {
    this.isLoadingConversations = true;
    this.chatService.getConversations().subscribe({
      next: (conversations) => {
        this.conversations = conversations;
        this.isLoadingConversations = false;
      },
      error: (error) => {
        console.error('Error loading conversations:', error);
        this.isLoadingConversations = false;
      }
    });
  }

  selectConversation(conversation: ChatConversation): void {
    // Unsubscribe from previous
    if (this.selectedConversation) {
      this.chatService.unsubscribeFromConversation(this.selectedConversation.id);
    }
    
    this.selectedConversation = conversation;
    this.replyingTo = null;
    this.loadMessages();
    
    // Subscribe to new conversation
    if (this.isConnected) {
      this.chatService.subscribeToConversation(conversation.id);
    }
    
    // Mark as read
    if (conversation.myUnreadCount && conversation.myUnreadCount > 0) {
      this.chatService.markAsRead(conversation.id).subscribe(() => {
        conversation.myUnreadCount = 0;
      });
    }
  }

  selectConversationById(conversationId: number): void {
    const conversation = this.conversations.find(c => c.id === conversationId);
    if (conversation) {
      this.selectConversation(conversation);
    } else {
      // Load the conversation
      this.chatService.getConversation(conversationId).subscribe({
        next: (conv) => {
          this.conversations.unshift(conv);
          this.selectConversation(conv);
        },
        error: (err) => console.error('Error loading conversation:', err)
      });
    }
  }

  loadMessages(): void {
    if (!this.selectedConversation) return;
    
    this.isLoadingMessages = true;
    this.chatService.getMessages(this.selectedConversation.id).subscribe({
      next: (page) => {
        this.messages = page.content.reverse(); // Oldest first
        this.isLoadingMessages = false;
        this.shouldScrollToBottom = true;
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.isLoadingMessages = false;
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.selectedConversation || this.isSending) return;
    
    this.isSending = true;
    const content = this.newMessage.trim();
    const replyToId = this.replyingTo?.id;
    
    const request = {
      conversationId: this.selectedConversation.id,
      content: content,
      replyToId: replyToId
    };
    
    this.chatService.sendMessage(request).subscribe({
      next: (message) => {
        this.messages.push(message);
        this.newMessage = '';
        this.replyingTo = null;
        this.isSending = false;
        this.shouldScrollToBottom = true;
        this.updateConversationLastMessage(message);
      },
      error: (error) => {
        console.error('Error sending message:', error);
        this.isSending = false;
      }
    });
  }

  onInputChange(): void {
    this.typingSubject.next();
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  replyTo(message: ChatMessage): void {
    this.replyingTo = message;
    this.messageInput?.nativeElement?.focus();
  }

  cancelReply(): void {
    this.replyingTo = null;
  }

  deleteMessage(message: ChatMessage): void {
    if (message.senderId !== this.currentUserId) return;
    
    this.chatService.deleteMessage(message.id).subscribe({
      next: () => {
        message.deleted = true;
        message.content = 'This message was deleted';
      },
      error: (err) => console.error('Error deleting message:', err)
    });
  }

  searchConversations(): void {
    if (!this.searchQuery.trim()) {
      this.loadConversations();
      return;
    }
    
    this.chatService.searchConversations(this.searchQuery).subscribe({
      next: (conversations) => {
        this.conversations = conversations;
      },
      error: (err) => console.error('Error searching:', err)
    });
  }

  private updateConversationLastMessage(message: ChatMessage): void {
    const conv = this.conversations.find(c => c.id === message.conversationId);
    if (conv) {
      conv.lastMessage = message.content;
      conv.lastMessageTime = message.sentAt;
      conv.lastMessageSenderId = message.senderId;
      
      // Move to top
      this.conversations = [conv, ...this.conversations.filter(c => c.id !== conv.id)];
    }
  }

  private scrollToBottom(): void {
    if (this.messagesContainer) {
      const container = this.messagesContainer.nativeElement;
      container.scrollTop = container.scrollHeight;
    }
  }

  getOtherParticipantName(conversation: ChatConversation): string {
    return conversation.otherParticipantName || 
           (this.currentUserRole === 'STUDENT' ? conversation.instructorName : conversation.studentName);
  }

  getMessageTime(dateString: string | null | undefined): string {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '';
      const now = new Date();
      const diff = now.getTime() - date.getTime();
      
      if (diff < 60000) return 'Just now';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
      if (diff < 86400000) return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      if (diff < 604800000) return date.toLocaleDateString([], { weekday: 'short' });
      return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
    } catch {
      return '';
    }
  }

  getFullDateTime(dateString: string | null | undefined): string {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '';
      return date.toLocaleString();
    } catch {
      return '';
    }
  }

  isOwnMessage(message: ChatMessage): boolean {
    return message.senderId === this.currentUserId;
  }

  getTypingText(): string {
    const users = Array.from(this.typingUsers.values());
    if (users.length === 0) return '';
    if (users.length === 1) return `${users[0]} is typing...`;
    return `${users.join(', ')} are typing...`;
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'SENT': return 'check';
      case 'DELIVERED': return 'done_all';
      case 'READ': return 'done_all';
      default: return 'schedule';
    }
  }

  getStatusColor(status: string): string {
    return status === 'READ' ? 'primary' : '';
  }

  // Safe string truncation helper - handles non-string values
  truncateText(text: any, maxLength: number): string {
    if (text == null) return '';
    const str = typeof text === 'string' ? text : String(text);
    if (str.length <= maxLength) return str;
    return str.slice(0, maxLength) + '...';
  }

  // Check if value is a valid non-empty string
  isValidString(value: any): boolean {
    return typeof value === 'string' && value.length > 0;
  }

  trackByConversation(index: number, conversation: ChatConversation): number {
    return conversation.id;
  }

  trackByMessage(index: number, message: ChatMessage): number {
    return message.id;
  }
}
