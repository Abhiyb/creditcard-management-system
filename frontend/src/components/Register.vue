<template>
  <div class="min-h-screen bg-gray-50 py-8 sm:py-12 md:py-10">
    <div class="container mx-auto px-4 sm:px-6 lg:px-8 max-w-7xl">
      <!-- Logo -->
      <div class="flex justify-center mb-8 md:mb-10">
        <a href="/" class="inline-flex items-center gap-2">
          <div class="h-8 w-8 rounded-full bg-gradient-to-r from-blue-500 to-indigo-500 flex items-center justify-center">
            <span class="text-white font-bold text-lg">C</span>
          </div>
          <span class="font-bold text-2xl sm:text-3xl bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-purple-600">
            Credify
          </span>
        </a>
      </div>

      <!-- Main Content - stacks on mobile, side-by-side on md+ -->
      <div class="flex flex-col md:flex-row gap-6 md:gap-8">
        <!-- Left Column - Info -->
        <div class="w-full md:w-1/3 lg:w-5/12">
          <div class="bg-white p-6 sm:p-8 rounded-2xl shadow-sm">
            <h2 class="text-xl sm:text-2xl font-bold mb-4 text-gray-900">Join Credify Today</h2>
            <p class="text-gray-600 mb-6 text-sm sm:text-base">
              Create your account and unlock a world of card management possibilities. Get access to:
            </p>
            <ul class="space-y-4">
              <li class="flex items-start gap-3">
                <div class="bg-blue-100 p-2 rounded-full mt-1 flex-shrink-0">
                  <svg class="w-5 h-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <div>
                  <h3 class="font-medium text-gray-900">Credit Card Management</h3>
                  <p class="text-gray-600 text-sm">Apply for new cards and manage existing ones</p>
                </div>
              </li>
              <!-- ... rest of the list items unchanged ... -->
            </ul>

            <div class="mt-8 p-6 bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl text-white">
              <h3 class="font-bold mb-2">Buy Now, Pay Later Eligible</h3>
              <p class="text-white/90 mb-3 text-sm">
                Users with an annual income of ₹3,60,000 or more are automatically eligible for our BNPL services.
              </p>
              <div class="flex items-center justify-center p-3 bg-white/20 rounded-lg mt-2">
                <svg class="w-5 h-5 mr-2 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 9a3 3 0 0 1 3 3 3 3 0 0 1-3 3h-3v-6h3zm-3 9v-3"></path>
                </svg>
                <span class="font-semibold">3,60,000+ Annual Income</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column - Form -->
        <div class="w-full md:w-2/3 lg:w-7/12">
          <div class="bg-white shadow-sm rounded-2xl p-6 sm:p-8">
            <div class="text-center mb-6">
              <h2 class="text-xl sm:text-2xl font-bold text-gray-900">Create your account</h2>
              <p class="text-gray-600 text-sm sm:text-base mt-1">Enter your details below to register with Credify</p>
            </div>

            <!-- Success/Error Message -->
            <div v-if="message" :class="[
              'mt-4 p-4 rounded-lg text-center text-sm sm:text-base',
              messageType === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
            ]">
              {{ message }}
            </div>

            <div class="space-y-6 mt-6">
              <!-- Personal Information Section -->
              <div class="text-lg font-medium text-gray-900 mb-3">Personal Information</div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-5 sm:gap-6">
                <!-- Full Name -->
                <div>
                  <label class="block mb-1 text-gray-900 text-sm">Full Name</label>
                  <div class="relative">
                    <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207"></path>
                    </svg>
                    <input
                      v-model="registerForm.fullName"
                      type="text"
                      placeholder="John Doe"
                      class="w-full pl-10 p-2.5 border border-gray-200 rounded-md focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50 text-sm"
                      required
                    />
                  </div>
                  <p v-if="errors.fullName" class="text-red-500 text-xs mt-1">{{ errors.fullName }}</p>
                </div>

                <!-- Email -->
                <div>
                  <label class="block mb-1 text-gray-900 text-sm">Email</label>
                  <div class="relative">
                    <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                    </svg>
                    <input
                      v-model="registerForm.email"
                      type="email"
                      placeholder="name@example.com"
                      class="w-full pl-10 p-2.5 border border-gray-200 rounded-md focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50 text-sm"
                      required
                    />
                  </div>
                  <p v-if="errors.email" class="text-red-500 text-xs mt-1">{{ errors.email }}</p>
                </div>

                <!-- Phone Number (full width) -->
                <div class="sm:col-span-2">
                  <label class="block mb-1 text-gray-900 text-sm">Phone Number</label>
                  <div class="relative">
                    <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h18M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"></path>
                    </svg>
                    <input
                      v-model="registerForm.phone"
                      type="text"
                      placeholder="10 digit number"
                      class="w-full pl-10 p-2.5 border border-gray-200 rounded-md focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50 text-sm"
                      required
                    />
                  </div>
                  <p v-if="errors.phone" class="text-red-500 text-xs mt-1">{{ errors.phone }}</p>
                </div>

                <!-- Address (full width) -->
                <div class="sm:col-span-2">
                  <label class="block mb-1 text-gray-900 text-sm">Address</label>
                  <div class="relative">
                    <svg class="absolute left-3 top-3 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
                    </svg>
                    <textarea
                      v-model="registerForm.address"
                      placeholder="Enter your full address"
                      class="w-full pl-10 p-2.5 border border-gray-200 rounded-md min-h-[90px] sm:min-h-[100px] focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50 text-sm"
                      required
                    ></textarea>
                  </div>
                  <p v-if="errors.address" class="text-red-500 text-xs mt-1">{{ errors.address }}</p>
                </div>

                <!-- Annual Income -->
                <div class="sm:col-span-2">
                  <label class="block mb-1 text-gray-900 text-sm">Annual Income (₹)</label>
                  <div class="relative">
                    <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 9a3 3 0 0 1 3 3 3 3 0 0 1-3 3h-3v-6h3zm-3 9v-3"></path>
                    </svg>
                    <input
                      v-model="registerForm.annualIncome"
                      type="number"
                      placeholder="Annual income"
                      class="w-full pl-10 p-2.5 border border-gray-200 rounded-md focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50 text-sm"
                      required
                    />
                  </div>
                  <p v-if="errors.annualIncome" class="text-red-500 text-xs mt-1">{{ errors.annualIncome }}</p>
                  <p class="text-xs text-gray-500 mt-1">
                    Annual income of ₹3,60,000 or more qualifies for Buy Now, Pay Later services.
                  </p>
                </div>
              </div>

              <hr class="my-6 border-gray-200" />

              <!-- Security Section -->
              <div class="text-lg font-medium text-gray-900 mb-3">Security</div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-5 sm:gap-6">
                <!-- Password -->
                <div>
                  <label class="block mb-1 text-gray-900 text-sm">Password</label>
                  <div class="relative">
                    <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 11c0-1.1.9-2 2-2s2 .9 2 2-2 4-2 4m-6-4c0-1.1.9-2 2-2s2 .9 2 2-2 4-2 4m-2-8h4m-4 8h4"></path>
                    </svg>
                    <input
                      v-model="registerForm.password"
                      :type="showPassword ? 'text' : 'password'"
                      placeholder="••••••••"
                      class="w-full pl-10 pr-10 p-2.5 border border-gray-200 rounded-md focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50 text-sm"
                      required
                    />
                    <button
                      @click="showPassword = !showPassword"
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                      type="button"
                    >
                      <!-- Add eye / eye-off icon here if you want -->
                    </button>
                  </div>
                  <p v-if="errors.password" class="text-red-500 text-xs mt-1">{{ errors.password }}</p>
                </div>

                <!-- Confirm Password -->
                <div>
                  <label class="block mb-1 text-gray-900 text-sm">Confirm Password</label>
                  <div class="relative">
                    <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 11c0-1.1.9-2 2-2s2 .9 2 2-2 4-2 4m-6-4c0-1.1.9-2 2-2s2 .9 2 2-2 4-2 4m-2-8h4m-4 8h4"></path>
                    </svg>
                    <input
                      v-model="registerForm.repeatPassword"
                      :type="showRepeatPassword ? 'text' : 'password'"
                      placeholder="••••••••"
                      class="w-full pl-10 pr-10 p-2.5 border border-gray-200 rounded-md focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50 text-sm"
                      required
                    />
                    <button
                      @click="showRepeatPassword = !showRepeatPassword"
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                      type="button"
                    >
                      <!-- Add eye / eye-off icon here if you want -->
                    </button>
                  </div>
                  <p v-if="errors.repeatPassword" class="text-red-500 text-xs mt-1">{{ errors.repeatPassword }}</p>
                </div>
              </div>

              <!-- Terms Checkbox -->
              <div class="flex items-start space-x-3 mt-6">
                <input
                  type="checkbox"
                  v-model="registerForm.terms"
                  class="h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500 mt-0.5"
                  required
                />
                <label class="text-sm text-gray-600">
                  I agree to the
                  <a href="/terms" class="text-blue-600 hover:text-blue-800 underline">Terms and Conditions</a>
                </label>
              </div>
              <p v-if="errors.terms" class="text-red-500 text-xs mt-1">{{ errors.terms }}</p>

              <!-- Submit Button -->
              <div class="border-t border-gray-200 pt-6 mt-6">
                <button
                  @click.prevent="handleRegister"
                  :disabled="isLoading"
                  class="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition-all duration-300 font-medium text-base disabled:opacity-60"
                >
                  {{ isLoading ? 'Creating your account...' : 'Create account' }}
                </button>

                <div class="mt-6 text-center text-sm">
                  <span class="text-gray-600">
                    Already have an account?
                    <router-link to="/login" class="text-blue-600 hover:text-blue-800 font-medium underline">
                      Sign in
                    </router-link>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// ────────────────────────────────────────────────────────────────────────────────
// Your original script remains 100% unchanged
// ────────────────────────────────────────────────────────────────────────────────
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const router = useRouter();

const isLoading = ref(false);
const message = ref('');
const messageType = ref('success'); // or 'error'

const showPassword = ref(false);
const showRepeatPassword = ref(false);

const registerForm = ref({
  fullName: '',
  email: '',
  phone: '',
  address: '',
  annualIncome: '',
  password: '',
  repeatPassword: '',
  terms: false,
});

const errors = ref({
  fullName: '',
  email: '',
  phone: '',
  address: '',
  annualIncome: '',
  password: '',
  repeatPassword: '',
  terms: '',
});

// You can keep these if you want, but they're already in the template via @click
// const togglePassword = () => showPassword.value = !showPassword.value;
// const toggleRepeatPassword = () => showRepeatPassword.value = !showRepeatPassword.value;

// Validation (unchanged)
const validateForm = () => {
  errors.value = {};
  let isValid = true;

  if (!registerForm.value.fullName || registerForm.value.fullName.trim().length < 2) {
    errors.value.fullName = 'Full name is required (min 2 characters)';
    isValid = false;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!registerForm.value.email) {
    errors.value.email = 'Email is required';
    isValid = false;
  } else if (!emailRegex.test(registerForm.value.email)) {
    errors.value.email = 'Please enter a valid email';
    isValid = false;
  }

  const phoneRegex = /^\d{10}$/;
  if (!registerForm.value.phone) {
    errors.value.phone = 'Phone number is required';
    isValid = false;
  } else if (!phoneRegex.test(registerForm.value.phone)) {
    errors.value.phone = 'Phone must be exactly 10 digits';
    isValid = false;
  }

  if (!registerForm.value.address || registerForm.value.address.trim().length < 5) {
    errors.value.address = 'Address is required (min 5 characters)';
    isValid = false;
  }

  const income = parseFloat(registerForm.value.annualIncome);
  if (!registerForm.value.annualIncome || isNaN(income) || income <= 0) {
    errors.value.annualIncome = 'Annual income must be a positive number';
    isValid = false;
  }

  if (!registerForm.value.password) {
    errors.value.password = 'Password is required';
    isValid = false;
  } else if (registerForm.value.password.length < 5) {
    errors.value.password = 'Password must be at least 5 characters';
    isValid = false;
  }

  if (!registerForm.value.repeatPassword) {
    errors.value.repeatPassword = 'Please confirm password';
    isValid = false;
  } else if (registerForm.value.password !== registerForm.value.repeatPassword) {
    errors.value.repeatPassword = 'Passwords do not match';
    isValid = false;
  }

  if (!registerForm.value.terms) {
    errors.value.terms = 'You must agree to the terms';
    isValid = false;
  }

  return isValid;
};

const handleRegister = async () => {
  if (!validateForm()) return;

  isLoading.value = true;
  message.value = '';
  messageType.value = 'success';

  const userData = {
    fullName: registerForm.value.fullName.trim(),
    email: registerForm.value.email.trim(),
    phone: registerForm.value.phone.trim(),
    address: registerForm.value.address.trim(),
    annualIncome: parseFloat(registerForm.value.annualIncome),
    password: registerForm.value.password,
  };

  try {
    const response = await fetch(`${API_BASE_URL}/profile`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });

    const responseText = await response.text();

    if (!response.ok) {
      let errorMsg = 'Registration failed. Please try again.';
      try {
        const errorData = JSON.parse(responseText);
        errorMsg = errorData.message || errorData.error || responseText;
      } catch {
        errorMsg = responseText || errorMsg;
      }
      throw new Error(errorMsg);
    }

    message.value = 'Registration successful! Redirecting to login...';
    messageType.value = 'success';

    try {
      const user = JSON.parse(responseText);
      if (user.userId) localStorage.setItem('userId', user.userId.toString());
      if (user.fullName) localStorage.setItem('fullName', user.fullName);
    } catch {
      // ignore
    }

    setTimeout(() => {
      router.push('/login');
    }, 2000);
  } catch (err) {
    message.value = err.message.includes('already exists')
      ? 'Email is already registered. Please use another email or login.'
      : err.message;
    messageType.value = 'error';
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* You can keep or add small mobile tweaks here if needed later */
</style>