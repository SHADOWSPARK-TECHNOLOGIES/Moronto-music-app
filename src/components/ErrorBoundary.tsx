import { Component, type ErrorInfo, type ReactNode } from 'react';
import { AlertTriangle, RotateCcw } from 'lucide-react';

interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  message: string;
}

export class ErrorBoundary extends Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  state: ErrorBoundaryState = {
    hasError: false,
    message: '',
  };

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return {
      hasError: true,
      message: error.message || 'An unexpected error occurred.',
    };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    console.error('Moronto Music ErrorBoundary caught:', error, info.componentStack);
  }

  render() {
    if (this.state.hasError) {
      let friendly = this.state.message;
      try {
        const parsed = JSON.parse(this.state.message) as { error?: string };
        if (parsed?.error) {
          friendly = parsed.error;
        }
      } catch {
        // Non-JSON error message
      }

      return (
        <div className="min-h-screen bg-[#071321] text-[#F4FAFE] flex items-center justify-center p-6">
          <div className="w-full max-w-md rounded-3xl bg-[#0B1727] border border-[#1E3552] p-6 space-y-4 text-center">
            <AlertTriangle className="w-10 h-10 text-[#FFBE55] mx-auto" aria-hidden="true" />
            <h1 className="text-xl font-extrabold">Something went wrong</h1>
            <p className="text-xs sm:text-sm text-[#A5B6CA] leading-relaxed break-words">
              {friendly}
            </p>
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="min-h-[44px] px-5 py-2.5 rounded-xl bg-[#12D9A1] text-[#04130E] text-xs font-extrabold inline-flex items-center gap-2"
            >
              <RotateCcw className="w-4 h-4" aria-hidden="true" />
              <span>Reload Moronto Music</span>
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
