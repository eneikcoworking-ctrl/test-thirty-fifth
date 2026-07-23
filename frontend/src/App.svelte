<script lang="ts">
  import { onMount } from 'svelte';

  // State Management with Svelte 5 Runes
  let token = $state(localStorage.getItem('bookmark_token') || '');
  let username = $state(localStorage.getItem('bookmark_username') || '');
  let currentView = $state(localStorage.getItem('bookmark_token') ? 'library' : 'login'); // 'login' | 'register' | 'library'

  // Form states for login/register
  let authUsername = $state('');
  let authEmail = $state('');
  let authPassword = $state('');
  let authError = $state('');
  let authLoading = $state(false);
  let showPassword = $state(false);

  // Bookmarks Data state
  let bookmarks = $state<any[]>([]);
  let searchQuery = $state('');
  let selectedTag = $state('All');
  let dataLoading = $state(false);

  // Dialog / Form states for Bookmark Add/Edit
  let showFormDialog = $state(false);
  let dialogMode = $state<'add' | 'edit'>('add');
  let editingBookmarkId = $state<number | null>(null);
  let formUrl = $state('');
  let formTitle = $state('');
  let formNotes = $state('');
  let formTags = $state(''); // Comma-separated input
  let formError = $state('');

  // Dropdown states for each bookmark card to show edit/delete options
  let activeMenuId = $state<number | null>(null);

  // Fallback storage key
  const LOCAL_STORAGE_KEY = 'bookmarks_fallback_data';

  // Seed default mockup data so the interface exactly mimics the design mockups on first load!
  const defaultMockupData = [
    {
      id: 1,
      userId: 42,
      url: "https://m3.material.io/foundations",
      title: "Google Design System",
      notes: "M3 Guidelines and Material foundations.",
      createdAt: "2026-07-22T23:30:00Z",
      tags: [
        { id: 101, name: "Design", createdAt: "2026-07-22T23:30:00Z" },
        { id: 102, name: "Guidelines", createdAt: "2026-07-22T23:30:00Z" }
      ],
      favicon: "https://lh3.googleusercontent.com/aida-public/AB6AXuD7PAcsi_askmso26fiQpi6dNHo81zZx5rnfSTTVtbYl9RjNG-3cdo5eX8Q5ejQk3SyeWvSh4YqGp_y_AhRPslOf8JTmrYOJyjqEMerjmzovrQnLbw7JV8vsd4FPIZ5JeBnsI1OnQnh-MInLbo88D9TOzCetHUiJGjX4uPU2sHRmaT2wM1brslldpQj3rsckgb9h2jGkkUj6MKZV7VughZy2coeq0gqd-weljwUQrOQcxH1EaIG9VNA3DA4hw2Ajd7OIEwwaBoqr5R_"
    },
    {
      id: 2,
      userId: 42,
      url: "https://github.com/ui-frameworks/main",
      title: "GitHub Repository",
      notes: "Main repository for our developer UI frameworks.",
      createdAt: "2026-07-22T23:25:00Z",
      tags: [
        { id: 103, name: "Work", createdAt: "2026-07-22T23:25:00Z" },
        { id: 104, name: "Code", createdAt: "2026-07-22T23:25:00Z" }
      ],
      favicon: "https://lh3.googleusercontent.com/aida-public/AB6AXuCQLaXn1H3vrlAY9IjGzr3xTCDcSBwSNptF4e2WkX2A6MYhVsxuBxCQADMTmYVhYHa_UhtpqT6YqUBN3nLcj6ydjZq8o11VFLGjBhqNI0V3xImF08MouSpbu9Tgzod__8P75HLdQ8p18wTzJwVKup3rkojjXpa0HRF9VX8kg7ymKKcqsiLaThNS7q1itu33xzn0leTV_I6mvW4RRAO5CJRebj5uQXI6Gt2sSKWGrNnrXUHAF4zDPCASI47DFwezHRwoh_6jk7WOXNjV"
    },
    {
      id: 3,
      userId: 42,
      url: "https://blog.photo.com/tutorials/landscapes",
      title: "Landscape Photography Tips",
      notes: "Beautiful composition tips and guidelines.",
      createdAt: "2026-07-22T23:20:00Z",
      tags: [
        { id: 105, name: "Tutorials", createdAt: "2026-07-22T23:20:00Z" }
      ],
      favicon: "https://lh3.googleusercontent.com/aida-public/AB6AXuDAP6HGQQBC3D_MbiBUg-BOLnyZT-UFbSazwuZ3_V2ab_GdnT_D1xH4o-O0lwR1VUNkslpEJ2x9izlApEaaSKQxTfsYJ3e2GpXC7YxCjK-P2VfndRGUO7WmohTwaF_z1330ya5zwHTX1aBTs7zpOLBPd-Gy5qQuyomV5Fm_F2m3Y7-yCtEz_Op5WRI_KNhq2AyyVYpTiB_AiCAtgQdLCw9EoyEiHKOnSjatl6YuY6ecf5E1P0tbJk7_wvcfIulNgatQBEVXJwYXjJuo",
      previewImage: "https://lh3.googleusercontent.com/aida-public/AB6AXuAF8wHI9y4C52MBzGEwOXp0_tft9Ruejl-Nacw62IUfaH8C15T-l-vsnuC83-DB4ZzW8ONKvAv6N_9Bcv12F967-pOaTX2zmVbJ4nSxhcNBbm8riH0lQmGjgB8VGdKFG-vwd3QWb4_oJp8bf7WZXXeICSeaB3yoTR3LH0nT9iphlzopcRzFgyWRom0APAhv1CdRrw1CNSUPHUEVjgXS6RjNYvuomNh-fwkeGcVU2atJf0sOZY0LmtJYnmfmQFKeS58WM2cBso8p0C8f"
    },
    {
      id: 4,
      userId: 42,
      url: "https://maps.google.com/coffee-spot",
      title: "Local Café Hours",
      notes: "Best quiet place to code nearby.",
      createdAt: "2026-07-22T23:15:00Z",
      tags: [
        { id: 106, name: "Personal", createdAt: "2026-07-22T23:15:00Z" }
      ],
      favicon: "https://lh3.googleusercontent.com/aida-public/AB6AXuBtSC7_ybjuWkAFR4v99C0acQT3AvMlOOOwW9uJANWqq_jxLdy3m8pZKrESG9Ck06ZXPnvS1wbgki4P8k-w0ojD8ysOOFcljL78jaKiCS1NGoqbaW8iDqcoFxkvwL3sG-_EGW9JJ__M_bQktMBW1nLiAG7FXT399P5Bjzsu4EbWkWSFs_0V964FTMYZVv3bzgDsiwaM22zgrw4TXYIPKN_t4EKxqk4Oub-BWZNAZvNPPSHI0djcQ2gAlcQ-_YHUcDmcugztn7u33Wiq"
    }
  ];

  // Load bookmarks on mount or when view changes
  onMount(() => {
    if (token) {
      loadBookmarks();
    }
  });

  // Base API caller that gracefully falls back to localStorage
  async function apiFetch(endpoint: string, options: RequestInit = {}) {
    // Inject bearer token
    const headers = {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      ...(options.headers || {})
    };

    try {
      const response = await fetch(endpoint, { ...options, headers });
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'API error' }));
        throw new Error(errorData.message || `HTTP ${response.status}`);
      }
      if (response.status === 204) {
        return null;
      }
      return await response.json();
    } catch (err: any) {
      console.warn(`Real API connection failed or not implemented (${err.message}). Performing fallback state operations.`, err);
      throw err; // Propagate for fallback recovery in parent catch block
    }
  }

  // Get fallbacks from localStorage
  function getFallbackBookmarks(): any[] {
    const dataStr = localStorage.getItem(LOCAL_STORAGE_KEY);
    if (!dataStr) {
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(defaultMockupData));
      return defaultMockupData;
    }
    return JSON.parse(dataStr);
  }

  function saveFallbackBookmarks(data: any[]) {
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(data));
  }

  // Auth Operations
  async function handleRegister(e: Event) {
    e.preventDefault();
    if (authUsername.length < 3) {
      authError = 'Username must be at least 3 characters.';
      return;
    }
    if (!authEmail) {
      authError = 'Email is required.';
      return;
    }
    if (authPassword.length < 6) {
      authError = 'Password must be at least 6 characters.';
      return;
    }

    authLoading = true;
    authError = '';

    try {
      await apiFetch('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username: authUsername, email: authEmail, password: authPassword })
      });
      // Automatically switch to login upon successful registration
      currentView = 'login';
      authError = 'Registered successfully! Please log in.';
    } catch (err: any) {
      // Fallback local registration support
      const registeredUsers = JSON.parse(localStorage.getItem('registered_users') || '[]');
      if (registeredUsers.some((u: any) => u.username === authUsername)) {
        authError = 'Username already exists.';
      } else if (registeredUsers.some((u: any) => u.email === authEmail)) {
        authError = 'Email already exists.';
      } else {
        registeredUsers.push({ username: authUsername, email: authEmail, password: authPassword });
        localStorage.setItem('registered_users', JSON.stringify(registeredUsers));
        currentView = 'login';
        authError = 'Registered successfully (Local Fallback)! Please log in.';
      }
    } finally {
      authLoading = false;
    }
  }

  async function handleLogin(e: Event) {
    e.preventDefault();
    authLoading = true;
    authError = '';

    try {
      const res = await apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username: authUsername, password: authPassword })
      });
      token = res.token;
      username = authUsername;
      localStorage.setItem('bookmark_token', token);
      localStorage.setItem('bookmark_username', username);
      currentView = 'library';
      loadBookmarks();
    } catch (err: any) {
      // Fallback local login support
      const registeredUsers = JSON.parse(localStorage.getItem('registered_users') || '[]');
      const matched = registeredUsers.find((u: any) => u.username === authUsername && u.password === authPassword);

      // Standalone fallback login: strictly require a registered user match (no bypass)
      if (matched) {
        token = 'mock-jwt-token-fallback';
        username = authUsername;
        localStorage.setItem('bookmark_token', token);
        localStorage.setItem('bookmark_username', username);
        currentView = 'library';
        loadBookmarks();
      } else {
        authError = 'Invalid username or password.';
      }
    } finally {
      authLoading = false;
    }
  }

  function handleLogout() {
    token = '';
    username = '';
    localStorage.removeItem('bookmark_token');
    localStorage.removeItem('bookmark_username');
    currentView = 'login';
    bookmarks = [];
  }

  // CRUD Bookmarks operations
  async function loadBookmarks() {
    dataLoading = true;
    try {
      // Build filters
      const params = new URLSearchParams();
      if (selectedTag && selectedTag !== 'All') {
        params.append('tags', selectedTag);
      }
      if (searchQuery) {
        params.append('title', searchQuery);
      }
      params.append('sort', 'createdAt,desc');

      const data = await apiFetch(`/api/bookmarks?${params.toString()}`);
      bookmarks = data;
    } catch (err) {
      // Standalone Fallback filtration and retrieval
      let localData = getFallbackBookmarks();

      // Sort newest-first (createdAt desc)
      localData.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

      // Filter by tag
      if (selectedTag && selectedTag !== 'All') {
        localData = localData.filter(b =>
          b.tags && b.tags.some((t: any) => t.name.toLowerCase() === selectedTag.toLowerCase())
        );
      }

      // Filter by title substring (case-insensitive) or tag names matches search query
      if (searchQuery) {
        const query = searchQuery.toLowerCase();
        localData = localData.filter(b =>
          b.title.toLowerCase().includes(query) ||
          b.url.toLowerCase().includes(query) ||
          (b.notes && b.notes.toLowerCase().includes(query)) ||
          (b.tags && b.tags.some((t: any) => t.name.toLowerCase().includes(query)))
        );
      }

      bookmarks = localData;
    } finally {
      dataLoading = false;
    }
  }

  // Handle Tag Selection / Clicking Tag Filter
  function handleTagSelect(tagName: string) {
    selectedTag = tagName;
    loadBookmarks();
  }

  // Derive unique tags currently existing to populate filter chips
  let allAvailableTags = $derived.by(() => {
    const list = getFallbackBookmarks();
    const tagNames = new Set<string>();
    list.forEach(b => {
      if (b.tags) {
        b.tags.forEach((t: any) => tagNames.add(t.name));
      }
    });
    return ['All', ...Array.from(tagNames)];
  });

  // Toggle Dropdown Options Menu
  function toggleOptionsMenu(e: Event, id: number) {
    e.stopPropagation();
    if (activeMenuId === id) {
      activeMenuId = null;
    } else {
      activeMenuId = id;
    }
  }

  // Close options dropdown on window click
  function closeAllMenus() {
    activeMenuId = null;
  }

  // Open Dialog Modal
  function openAddDialog() {
    dialogMode = 'add';
    editingBookmarkId = null;
    formUrl = '';
    formTitle = '';
    formNotes = '';
    formTags = '';
    formError = '';
    showFormDialog = true;
  }

  function openEditDialog(bookmark: any) {
    dialogMode = 'edit';
    editingBookmarkId = bookmark.id;
    formUrl = bookmark.url;
    formTitle = bookmark.title;
    formNotes = bookmark.notes || '';
    formTags = bookmark.tags ? bookmark.tags.map((t: any) => t.name).join(', ') : '';
    formError = '';
    showFormDialog = true;
    activeMenuId = null; // Close menu
  }

  // Form submission for Add/Edit
  async function handleSaveBookmark(e: Event) {
    e.preventDefault();
    if (!formUrl) {
      formError = 'URL is required.';
      return;
    }
    if (!formTitle) {
      formError = 'Title is required.';
      return;
    }

    const tagList = formTags
      .split(',')
      .map(t => t.trim())
      .filter(t => t.length > 0);

    const payload = {
      url: formUrl,
      title: formTitle,
      notes: formNotes,
      tags: tagList
    };

    try {
      if (dialogMode === 'add') {
        await apiFetch('/api/bookmarks', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
      } else if (editingBookmarkId !== null) {
        await apiFetch(`/api/bookmarks/${editingBookmarkId}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
      }
      showFormDialog = false;
      loadBookmarks();
    } catch (err) {
      // Local Fallback Storage CRUD
      let localData = getFallbackBookmarks();
      const nowString = new Date().toISOString();

      const resolvedTags = tagList.map((tagName, index) => ({
        id: Math.floor(Math.random() * 100000) + index,
        name: tagName,
        createdAt: nowString
      }));

      if (dialogMode === 'add') {
        const newBookmark = {
          id: Date.now(),
          userId: 42,
          url: formUrl,
          title: formTitle,
          notes: formNotes,
          createdAt: nowString,
          tags: resolvedTags,
          favicon: `https://www.google.com/s2/favicons?domain=${new URL(formUrl).hostname}&sz=64`
        };
        localData.unshift(newBookmark); // Put newest first
      } else if (editingBookmarkId !== null) {
        localData = localData.map(b => {
          if (b.id === editingBookmarkId) {
            return {
              ...b,
              url: formUrl,
              title: formTitle,
              notes: formNotes,
              tags: resolvedTags
            };
          }
          return b;
        });
      }

      saveFallbackBookmarks(localData);
      showFormDialog = false;
      loadBookmarks();
    }
  }

  // Delete Bookmark
  async function handleDeleteBookmark(id: number) {
    if (!confirm('Are you sure you want to delete this bookmark?')) return;
    try {
      await apiFetch(`/api/bookmarks/${id}`, {
        method: 'DELETE'
      });
      loadBookmarks();
    } catch (err) {
      // Local Fallback Storage Delete
      let localData = getFallbackBookmarks();
      localData = localData.filter(b => b.id !== id);
      saveFallbackBookmarks(localData);
      loadBookmarks();
    } finally {
      activeMenuId = null;
    }
  }

  // Search submission helper
  function handleSearchSubmit(e: Event) {
    e.preventDefault();
    loadBookmarks();
  }
</script>

<svelte:window onclick={closeAllMenus} />

<div class="min-h-screen flex flex-col bg-surface text-on-surface">
  {#if currentView === 'login' || currentView === 'register'}
    <!-- Authenticated Portal View -->
    <header class="fixed top-0 left-0 right-0 z-50 bg-surface dark:bg-on-surface border-b border-outline-variant/10">
      <div class="flex items-center px-4 h-14 w-full max-w-[420px] mx-auto">
        <h1 class="font-headline-md text-headline-md font-bold text-primary dark:text-inverse-primary flex items-center gap-2">
          <span class="material-symbols-outlined text-[24px]">security</span>
          Security Portal
        </h1>
      </div>
    </header>

    <main class="flex-grow flex items-center justify-center px-4 pt-14 pb-24">
      <div class="w-full max-w-[420px] space-y-6 mt-8">
        <!-- Branding / Hero Section -->
        <section class="text-center space-y-2">
          <div class="w-20 h-20 bg-primary-container text-white rounded-xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-primary/10">
            <span class="material-symbols-outlined text-4xl" style="font-variation-settings: 'FILL' 1;">lock</span>
          </div>
          <h2 class="text-[1.5rem] font-bold tracking-tight text-on-surface">
            {currentView === 'login' ? 'Welcome Back' : 'Create Account'}
          </h2>
          <p class="text-on-surface-variant text-[0.875rem]">
            {currentView === 'login' ? 'Access your encrypted dashboard securely.' : 'Register to manage your secure bookmarks.'}
          </p>
        </section>

        <!-- Auth Form -->
        <form class="space-y-4" onsubmit={currentView === 'login' ? handleLogin : handleRegister}>
          {#if authError}
            <div class="p-3 bg-error-container text-on-error-container rounded-lg text-[0.875rem]" role="alert">
              {authError}
            </div>
          {/if}

          <!-- Username Input -->
          <div class="space-y-1">
            <label class="block text-[0.875rem] font-semibold text-on-surface-variant" for="username">Username</label>
            <input
              type="text"
              id="username"
              name="username"
              autocomplete="username"
              bind:value={authUsername}
              placeholder="e.g. john_doe"
              class="w-full h-12 px-4 bg-white border border-outline-variant rounded-lg text-[1rem] text-on-surface placeholder:text-outline/50 transition-all focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
              required
            />
          </div>

          {#if currentView === 'register'}
            <!-- Email Input -->
            <div class="space-y-1">
              <label class="block text-[0.875rem] font-semibold text-on-surface-variant" for="email">Email Address</label>
              <input
                type="email"
                id="email"
                name="email"
                autocomplete="email"
                bind:value={authEmail}
                placeholder="name@company.com"
                class="w-full h-12 px-4 bg-white border border-outline-variant rounded-lg text-[1rem] text-on-surface placeholder:text-outline/50 transition-all focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
                required
              />
            </div>
          {/if}

          <!-- Password Input -->
          <div class="space-y-1">
            <div class="flex justify-between items-end">
              <label class="block text-[0.875rem] font-semibold text-on-surface-variant" for="password">Password</label>
            </div>
            <div class="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                name="password"
                autocomplete="current-password"
                bind:value={authPassword}
                placeholder="••••••••"
                class="w-full h-12 pl-4 pr-12 bg-white border border-outline-variant rounded-lg text-[1rem] text-on-surface placeholder:text-outline/50 transition-all focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
                required
              />
              <button
                type="button"
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                onclick={() => showPassword = !showPassword}
                class="absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant hover:text-primary transition-colors flex items-center justify-center p-1 rounded-full"
              >
                <span class="material-symbols-outlined text-[20px]">
                  {showPassword ? 'visibility' : 'visibility_off'}
                </span>
              </button>
            </div>
          </div>

          <!-- Action Button -->
          <button
            type="submit"
            disabled={authLoading}
            class="w-full h-12 bg-primary text-on-primary rounded-lg text-[0.875rem] font-semibold shadow-lg shadow-primary/20 hover:bg-primary/90 active:scale-[0.98] transition-all flex items-center justify-center gap-2"
          >
            {#if authLoading}
              <span class="animate-ping w-2 h-2 rounded-full bg-white"></span>
              Processing...
            {:else}
              {currentView === 'login' ? 'Sign In' : 'Register Now'}
              <span class="material-symbols-outlined text-[20px]">login</span>
            {/if}
          </button>
        </form>

        <!-- Toggle Auth view -->
        <p class="text-center text-[0.875rem] text-on-surface-variant">
          {currentView === 'login' ? "Don't have an account?" : "Already have an account?"}
          <button
            type="button"
            onclick={() => {
              currentView = currentView === 'login' ? 'register' : 'login';
              authError = '';
            }}
            class="text-primary font-semibold hover:underline ml-1"
          >
            {currentView === 'login' ? 'Sign Up' : 'Sign In'}
          </button>
        </p>
      </div>
    </main>

    <!-- Bottom Navigation Bar for login switcher -->
    <nav class="fixed bottom-0 left-0 w-full z-50 px-4 py-3 bg-surface-container dark:bg-inverse-surface border-t border-outline-variant/10 flex justify-around items-center">
      <button
        type="button"
        onclick={() => { currentView = 'login'; authError = ''; }}
        class="flex flex-col items-center justify-center rounded-full px-5 py-1.5 transition-all {currentView === 'login' ? 'bg-secondary-container text-on-secondary-container' : 'text-on-surface-variant hover:opacity-80'}"
      >
        <span class="material-symbols-outlined">login</span>
        <span class="text-[0.75rem] font-semibold tracking-wide mt-0.5">Login</span>
      </button>
      <button
        type="button"
        onclick={() => { currentView = 'register'; authError = ''; }}
        class="flex flex-col items-center justify-center rounded-full px-5 py-1.5 transition-all {currentView === 'register' ? 'bg-secondary-container text-on-secondary-container' : 'text-on-surface-variant hover:opacity-80'}"
      >
        <span class="material-symbols-outlined">person_add</span>
        <span class="text-[0.75rem] font-semibold tracking-wide mt-0.5">Register</span>
      </button>
    </nav>
  {:else}
    <!-- Main Bookmarks Library View -->
    <header class="w-full top-0 sticky z-40 flex items-center justify-between px-4 h-16 bg-surface glass-header border-b border-outline-variant/10">
      <div class="flex items-center gap-3">
        <span class="material-symbols-outlined text-primary text-[28px]">bookmarks</span>
        <h1 class="text-[1.25rem] font-bold text-primary">Bookmarks</h1>
      </div>
      <div class="flex items-center gap-2">
        <span class="text-[0.875rem] text-on-surface-variant font-semibold">Hi, {username}</span>
        <button
          onclick={handleLogout}
          aria-label="Logout"
          class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container-low transition-colors text-error"
        >
          <span class="material-symbols-outlined">logout</span>
        </button>
      </div>
    </header>

    <main class="px-4 pb-32 max-w-[768px] mx-auto w-full">
      <!-- Search & Filters -->
      <section class="mt-4 mb-6">
        <form onsubmit={handleSearchSubmit} class="relative flex items-center mb-4">
          <span class="material-symbols-outlined absolute left-4 text-outline" aria-hidden="true">search</span>
          <input
            type="text"
            bind:value={searchQuery}
            oninput={() => loadBookmarks()}
            placeholder="Search saved links..."
            aria-label="Search saved links"
            class="w-full h-12 pl-12 pr-4 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary text-[1rem] outline-none transition-all"
          />
        </form>

        <!-- Dynamic Filter Chips -->
        <div class="flex gap-2 overflow-x-auto no-scrollbar pb-2" role="group" aria-label="Filter tags">
          {#each allAvailableTags as tag}
            <button
              type="button"
              onclick={() => handleTagSelect(tag)}
              class="h-8 px-4 rounded-full font-semibold text-[0.75rem] whitespace-nowrap transition-all {selectedTag === tag ? 'bg-primary text-on-primary shadow-sm' : 'bg-surface-container text-on-surface-variant hover:opacity-80'}"
            >
              {tag}
            </button>
          {/each}
        </div>
      </section>

      <!-- Bookmark List (Sorted Newest-First) -->
      <div class="space-y-4">
        {#if dataLoading}
          <div class="p-8 text-center text-outline flex flex-col items-center justify-center gap-2">
            <span class="animate-spin material-symbols-outlined text-3xl text-primary">autorenew</span>
            <span>Loading bookmarks...</span>
          </div>
        {:else if bookmarks.length === 0}
          <div class="p-12 text-center text-outline bg-surface-container-low rounded-lg border border-dashed border-outline-variant/50">
            <span class="material-symbols-outlined text-4xl mb-2 text-outline/50">bookmark_border</span>
            <p class="text-[1rem] font-semibold">No bookmarks found</p>
            <p class="text-[0.875rem] text-outline/70 mt-1">Try matching another tag, search term, or click + to add one.</p>
          </div>
        {:else}
          {#each bookmarks as bookmark (bookmark.id)}
            <div class="group relative bg-surface-container-lowest rounded-lg p-4 border border-outline-variant hover:shadow-md transition-all duration-200">
              <!-- Visual Card Header/Image optionally included as in Mockup -->
              {#if bookmark.previewImage}
                <div class="w-full aspect-video bg-cover bg-center rounded-t-lg mb-4 -mx-4 -mt-4 overflow-hidden" style="background-image: url('{bookmark.previewImage}')"></div>
              {/if}

              <div class="flex gap-4 items-start">
                <!-- Favicon Wrapper -->
                <div class="w-12 h-12 rounded-lg bg-surface-container flex items-center justify-center flex-shrink-0 overflow-hidden">
                  {#if bookmark.favicon}
                    <img class="w-8 h-8 object-contain" alt="" src={bookmark.favicon} />
                  {:else}
                    <span class="material-symbols-outlined text-outline">link</span>
                  {/if}
                </div>

                <!-- Text Content -->
                <div class="flex-grow min-w-0">
                  <div class="flex justify-between items-start gap-2">
                    <h3 class="font-bold text-[1.25rem] text-on-surface leading-tight truncate">
                      {bookmark.title}
                    </h3>

                    <!-- Actions dropdown triggers -->
                    <div class="relative">
                      <button
                        type="button"
                        aria-label="Bookmark options"
                        onclick={(e) => toggleOptionsMenu(e, bookmark.id)}
                        class="text-outline hover:text-primary transition-colors p-1 rounded-full hover:bg-surface-container-low"
                      >
                        <span class="material-symbols-outlined">more_vert</span>
                      </button>

                      {#if activeMenuId === bookmark.id}
                        <!-- Dropdown panel -->
                        <div class="absolute right-0 top-8 w-32 bg-white rounded-lg shadow-lg border border-outline-variant/50 py-1 z-30" role="menu">
                          <button
                            type="button"
                            role="menuitem"
                            onclick={() => openEditDialog(bookmark)}
                            class="w-full text-left px-4 py-2 text-[0.875rem] text-on-surface hover:bg-surface-container transition-colors flex items-center gap-2"
                          >
                            <span class="material-symbols-outlined text-[16px]">edit</span>
                            Edit
                          </button>
                          <button
                            type="button"
                            role="menuitem"
                            onclick={() => handleDeleteBookmark(bookmark.id)}
                            class="w-full text-left px-4 py-2 text-[0.875rem] text-error hover:bg-error-container/20 transition-colors flex items-center gap-2"
                          >
                            <span class="material-symbols-outlined text-[16px] text-error">delete</span>
                            Delete
                          </button>
                        </div>
                      {/if}
                    </div>
                  </div>

                  <a href={bookmark.url} target="_blank" rel="noopener noreferrer" class="text-[0.875rem] text-primary hover:underline truncate block mb-1">
                    {bookmark.url}
                  </a>

                  {#if bookmark.notes}
                    <p class="text-[0.875rem] text-outline line-clamp-2 mt-1 whitespace-pre-line">
                      {bookmark.notes}
                    </p>
                  {/if}

                  {#if bookmark.tags && bookmark.tags.length > 0}
                    <div class="flex flex-wrap gap-2 mt-2">
                      {#each bookmark.tags as t}
                        <span class="px-2 py-0.5 rounded bg-surface-container-highest text-primary font-semibold text-[0.75rem]">
                          {t.name}
                        </span>
                      {/each}
                    </div>
                  {/if}
                </div>
              </div>
            </div>
          {/each}
        {/if}
      </div>
    </main>

    <!-- Contextual Floating Action Button (FAB) for Add -->
    <button
      onclick={openAddDialog}
      aria-label="Add Bookmark"
      class="fixed bottom-24 right-4 w-14 h-14 bg-primary text-on-primary rounded-full shadow-lg flex items-center justify-center transition-all duration-200 active:scale-90 hover:brightness-110 z-30 focus:ring-4 focus:ring-primary/40 focus:outline-none"
    >
      <span class="material-symbols-outlined text-[32px]">add</span>
    </button>

    <!-- Bottom Navigation Bar for main view -->
    <nav class="fixed bottom-0 left-0 w-full z-40 flex justify-around items-center px-4 py-3 pb-safe bg-surface-container dark:bg-inverse-surface shadow-md">
      <button
        type="button"
        onclick={() => handleTagSelect('All')}
        class="flex flex-col items-center justify-center rounded-full px-5 py-1.5 transition-all {selectedTag === 'All' ? 'bg-secondary-container text-on-secondary-container' : 'text-on-surface-variant hover:opacity-80'}"
      >
        <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">folder_special</span>
        <span class="text-[0.75rem] font-semibold tracking-wide">Library</span>
      </button>
      <button
        type="button"
        onclick={() => alert('Collections features will be available in the next release!')}
        class="flex flex-col items-center justify-center text-on-surface-variant hover:opacity-80 transition-all"
      >
        <span class="material-symbols-outlined">layers</span>
        <span class="text-[0.75rem] font-semibold tracking-wide">Collections</span>
      </button>
      <button
        type="button"
        onclick={() => alert('Shared links features will be available in the next release!')}
        class="flex flex-col items-center justify-center text-on-surface-variant hover:opacity-80 transition-all"
      >
        <span class="material-symbols-outlined">group</span>
        <span class="text-[0.75rem] font-semibold tracking-wide">Shared</span>
      </button>
    </nav>
  {/if}

  <!-- Dialog Backdrop & Modal -->
  {#if showFormDialog}
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm" role="dialog" aria-modal="true" aria-labelledby="dialog-title">
      <div class="bg-white w-full max-w-[480px] rounded-xl shadow-xl overflow-hidden flex flex-col p-6 space-y-4">
        <div class="flex justify-between items-center border-b border-outline-variant/20 pb-3">
          <h2 id="dialog-title" class="text-[1.25rem] font-bold text-on-surface">
            {dialogMode === 'add' ? 'Add New Bookmark' : 'Edit Bookmark'}
          </h2>
          <button type="button" aria-label="Close dialog" onclick={() => showFormDialog = false} class="text-outline hover:text-on-surface transition-colors p-1 rounded-full">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        {#if formError}
          <div class="p-3 bg-error-container text-on-error-container rounded-lg text-[0.875rem]">
            {formError}
          </div>
        {/if}

        <form onsubmit={handleSaveBookmark} class="space-y-4">
          <!-- URL Input -->
          <div class="space-y-1">
            <label class="block text-[0.875rem] font-semibold text-on-surface-variant" for="form-url">URL</label>
            <input
              type="url"
              id="form-url"
              placeholder="https://example.com"
              bind:value={formUrl}
              required
              class="w-full h-12 px-4 bg-surface-container-low border border-outline-variant rounded-lg text-[1rem] focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
            />
          </div>

          <!-- Title Input -->
          <div class="space-y-1">
            <label class="block text-[0.875rem] font-semibold text-on-surface-variant" for="form-title">Title</label>
            <input
              type="text"
              id="form-title"
              placeholder="e.g. My Favorite Link"
              bind:value={formTitle}
              required
              class="w-full h-12 px-4 bg-surface-container-low border border-outline-variant rounded-lg text-[1rem] focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
            />
          </div>

          <!-- Notes Input -->
          <div class="space-y-1">
            <label class="block text-[0.875rem] font-semibold text-on-surface-variant" for="form-notes">Notes (Optional)</label>
            <textarea
              id="form-notes"
              placeholder="Add some notes or details..."
              bind:value={formNotes}
              rows="3"
              class="w-full p-4 bg-surface-container-low border border-outline-variant rounded-lg text-[1rem] focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary resize-none"
            ></textarea>
          </div>

          <!-- Tags Input -->
          <div class="space-y-1">
            <label class="block text-[0.875rem] font-semibold text-on-surface-variant" for="form-tags">Tags (Optional, comma separated)</label>
            <input
              type="text"
              id="form-tags"
              placeholder="e.g. tech, design, resources"
              bind:value={formTags}
              class="w-full h-12 px-4 bg-surface-container-low border border-outline-variant rounded-lg text-[1rem] focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
            />
          </div>

          <div class="flex justify-end gap-3 pt-4 border-t border-outline-variant/20">
            <button
              type="button"
              onclick={() => showFormDialog = false}
              class="px-5 h-12 bg-surface-container text-on-surface-variant font-semibold rounded-lg text-[0.875rem] hover:opacity-80 active:scale-95 transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="px-6 h-12 bg-primary text-on-primary font-semibold rounded-lg text-[0.875rem] shadow-lg shadow-primary/20 hover:brightness-110 active:scale-95 transition-all"
            >
              Save Bookmark
            </button>
          </div>
        </form>
      </div>
    </div>
  {/if}
</div>

<style>
  /* Ensure smooth tag horizontal scroll but hide standard scrollbars */
  .no-scrollbar::-webkit-scrollbar {
    display: none;
  }
  .no-scrollbar {
    -ms-overflow-style: none;
    scrollbar-width: none;
  }
</style>
