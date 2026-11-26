import React from 'react';
import { LucideIcon } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  variant?: 'blue' | 'red' | 'green' | 'amber' | 'default';
  trend?: {
    label: string;
    positive?: boolean;
  };
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtitle,
  icon: Icon,
  variant = 'default',
  trend
}) => {
  const getVariantStyles = () => {
    switch (variant) {
      case 'red':
        return {
          border: 'border-red-200 hover:border-red-300',
          iconBg: 'bg-red-50 text-red-600',
          valueColor: 'text-red-600'
        };
      case 'blue':
        return {
          border: 'border-blue-200 hover:border-blue-300',
          iconBg: 'bg-blue-50 text-blue-600',
          valueColor: 'text-blue-600'
        };
      case 'green':
        return {
          border: 'border-emerald-200 hover:border-emerald-300',
          iconBg: 'bg-emerald-50 text-emerald-600',
          valueColor: 'text-emerald-600'
        };
      case 'amber':
        return {
          border: 'border-amber-200 hover:border-amber-300',
          iconBg: 'bg-amber-50 text-amber-600',
          valueColor: 'text-amber-600'
        };
      case 'default':
      default:
        return {
          border: 'border-slate-200 hover:border-slate-300',
          iconBg: 'bg-slate-100 text-slate-700',
          valueColor: 'text-slate-800'
        };
    }
  };

  const styles = getVariantStyles();

  return (
    <div
      className={`bg-white rounded-xl p-5 border ${styles.border} shadow-xs transition-all hover:shadow-md hover:translate-y-[-2px]`}
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold tracking-wider uppercase text-slate-500 mb-1">{title}</p>
          <h3 className={`text-2xl font-bold font-mono ${styles.valueColor}`}>{value}</h3>
        </div>
        <div className={`p-2.5 rounded-lg ${styles.iconBg}`}>
          <Icon className="w-5 h-5" />
        </div>
      </div>

      {(subtitle || trend) && (
        <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
          {subtitle && <span className="text-slate-500">{subtitle}</span>}
          {trend && (
            <span
              className={`font-semibold ${
                trend.positive ? 'text-emerald-600' : 'text-slate-500'
              }`}
            >
              {trend.label}
            </span>
          )}
        </div>
      )}
    </div>
  );
};
