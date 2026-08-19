import React from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingSpinnerProps {
  message?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ message = 'Loading evidence data...' }) => (
  <div className="flex flex-col items-center justify-center p-12 text-slate-400">
    <Loader2 className="w-8 h-8 animate-spin text-indigo-500 mb-3" />
    <span className="text-sm font-medium">{message}</span>
  </div>
);
