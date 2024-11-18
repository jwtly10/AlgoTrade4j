import { config } from '@/config';

export function applyDefaultSettings(settings) {
  return {
    primaryColor: config.site.primaryColor,
    direction: 'ltr',
    navColor: 'evident',
    layout: 'vertical',
    language: config.site.language,
    ...settings,
  };
}
