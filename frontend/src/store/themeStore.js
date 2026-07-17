import { create } from 'zustand';

const useThemeStore = create((set) => ({
  theme: localStorage.getItem('theme') || 'dark',
  toggleTheme: () => set((state) => {
    const newTheme = state.theme === 'dark' ? 'light' : 'dark';
    localStorage.setItem('theme', newTheme);
    document.documentElement.className = `${newTheme}-theme`;
    document.documentElement.setAttribute('data-theme', newTheme);
    return { theme: newTheme };
  }),
  initTheme: () => {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    document.documentElement.className = `${savedTheme}-theme`;
    document.documentElement.setAttribute('data-theme', savedTheme);
    set({ theme: savedTheme });
  }
}));

export default useThemeStore;
