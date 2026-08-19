import React from 'react';
import { LayoutDashboard, Compass, HelpCircle, TrendingUp, Lightbulb, User } from 'lucide-react';
import { Student } from '../types/api';

interface NavbarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  students: Student[];
  currentStudent: Student | null;
  onSelectStudent: (student: Student) => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  students,
  currentStudent,
  onSelectStudent,
}) => {
  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'explorer', label: 'Concept Explorer', icon: Compass },
    { id: 'assessment', label: 'Assessment', icon: HelpCircle },
    { id: 'progress', label: 'Progress', icon: TrendingUp },
    { id: 'recommendation', label: 'Recommendation', icon: Lightbulb },
  ];

  return (
    <header className="bg-slate-900/80 backdrop-blur border-b border-slate-800 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between h-16">
        {/* Brand */}
        <div className="flex items-center gap-3">
          <div className="h-9 w-9 rounded-lg bg-indigo-600 flex items-center justify-center font-bold text-white shadow-lg shadow-indigo-500/20">
            KG
          </div>
          <div>
            <h1 className="text-base font-bold text-slate-100 leading-none">Knowledge Gap</h1>
            <span className="text-xs text-indigo-400 font-medium">Intelligence Platform</span>
          </div>
        </div>

        {/* Nav Links */}
        <nav className="flex items-center space-x-1 sm:space-x-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-indigo-600/20 text-indigo-400 border border-indigo-500/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span className="hidden md:inline">{item.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Student Selector */}
        <div className="flex items-center gap-2 bg-slate-800/80 px-3 py-1.5 rounded-lg border border-slate-700">
          <User className="w-4 h-4 text-indigo-400" />
          <select
            value={currentStudent?.id || ''}
            onChange={(e) => {
              const selected = students.find((s) => s.id === e.target.value);
              if (selected) onSelectStudent(selected);
            }}
            className="bg-transparent text-sm text-slate-200 focus:outline-none cursor-pointer"
          >
            {students.map((s) => (
              <option key={s.id} value={s.id} className="bg-slate-800 text-slate-200">
                {s.name} ({s.email})
              </option>
            ))}
          </select>
        </div>
      </div>
    </header>
  );
};
