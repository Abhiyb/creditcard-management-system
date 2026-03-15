<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-50 to-blue-50 py-8 px-4 sm:py-10 sm:px-6">
    <div class="max-w-5xl mx-auto bg-white rounded-3xl shadow-xl overflow-hidden">
      <!-- Header -->
      <div class="bg-gradient-to-r from-purple-600 to-indigo-600 p-6 sm:p-8 text-white">
        <h2 class="text-2xl sm:text-3xl md:text-4xl font-bold">Apply for a New Card</h2>
        <p class="text-base sm:text-lg mt-2 opacity-90">Step 1: Choose your perfect card</p>
      </div>

      <!-- Main Form Area -->
      <div class="p-6 sm:p-8 md:p-12">
        <!-- Loading -->
        <div v-if="loading" class="flex justify-center items-center py-16 sm:py-20">
          <div class="animate-spin rounded-full h-10 w-10 sm:h-12 sm:w-12 border-b-2 border-purple-600"></div>
          <span class="ml-4 text-gray-700 font-medium">Submitting your application...</span>
        </div>

        <!-- Error -->
        <div v-else-if="errorMessage" class="text-center py-12">
          <div class="bg-red-50 border border-red-200 text-red-700 px-5 sm:px-6 py-6 sm:py-8 rounded-xl max-w-2xl mx-auto">
            <p class="text-lg font-semibold">Oops!</p>
            <p class="mt-2">{{ errorMessage }}</p>
            <button
              @click="resetForm"
              class="mt-6 bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-lg transition text-base sm:text-lg"
            >
              Try Again
            </button>
          </div>
        </div>

        <!-- Success -->
        <div v-else-if="successMessage" class="text-center py-12 sm:py-16">
          <div class="bg-green-50 border border-green-200 text-green-800 px-5 sm:px-6 py-8 sm:py-10 rounded-xl max-w-2xl mx-auto">
            <svg class="w-14 h-14 sm:w-16 sm:h-16 text-green-500 mx-auto mb-4 sm:mb-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p class="text-xl sm:text-2xl font-bold mb-3">Application Submitted!</p>
            <p class="text-base sm:text-lg mb-4 sm:mb-6">{{ successMessage }}</p>
            <p class="text-gray-600 text-sm sm:text-base">Redirecting to My Cards in 3 seconds...</p>
          </div>
        </div>

        <!-- Form -->
        <div v-else>
          <h3 class="text-xl sm:text-2xl font-semibold text-gray-800 mb-6 text-center md:text-left">
            Select Your Card Type
          </h3>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-5 sm:gap-6 mb-8 sm:mb-10">
            <div
              v-for="card in cards"
              :key="card.id"
              @click="selectCard(card)"
              :class="[
                'cursor-pointer rounded-2xl overflow-hidden border-2 transition-all duration-300 hover:shadow-xl hover:scale-[1.02] active:scale-[0.98]',
                selectedCard?.id === card.id
                  ? 'border-purple-500 bg-purple-50/50 ring-2 ring-purple-300'
                  : 'border-gray-200 hover:border-purple-300'
              ]"
            >
              <div :style="getCardBackground(card.cardType)" class="h-44 sm:h-48 px-5 sm:px-6 py-5 sm:py-6 text-white relative rounded-t-2xl">
                <div class="flex justify-between items-start">
                  <span class="text-xl sm:text-2xl font-bold tracking-wider drop-shadow-md">
                    {{ getCardLogoText(card.cardType) }}
                  </span>
                  <div class="w-10 h-8 sm:w-12 sm:h-9 bg-yellow-400 rounded-md flex items-center justify-center text-black font-bold text-xs shadow-md">
                    CHIP
                  </div>
                </div>
                <div class="mt-6 sm:mt-8">
                  <p class="text-xl sm:text-2xl font-mono tracking-widest drop-shadow-md">
                    **** **** **** 1234
                  </p>
                </div>
                <div class="mt-5 sm:mt-6 flex justify-between text-xs sm:text-sm drop-shadow-md">
                  <div>
                    <div class="text-xs opacity-80">VALID THRU</div>
                    <div class="text-sm sm:text-base">12/30</div>
                  </div>
                  <div class="text-lg sm:text-xl font-bold uppercase tracking-wider opacity-90">
                    {{ getCardLogoText(card.cardType) }}
                  </div>
                </div>
              </div>

              <div class="p-5 sm:p-6 bg-white rounded-b-2xl">
                <h4 class="font-bold text-base sm:text-lg text-gray-800 mb-1 sm:mb-2">{{ card.title }}</h4>
                <p class="text-xs sm:text-sm text-gray-600">{{ card.description }}</p>
              </div>
            </div>
          </div>

          <!-- Credit Limit -->
          <div class="max-w-md mx-auto md:mx-0">
            <label class="block text-gray-700 font-medium mb-2 sm:mb-3 text-base sm:text-lg">
              Requested Credit Limit (₹)
            </label>
            <input
              v-model.number="requestedLimit"
              type="number"
              min="10000"
              step="1000"
              placeholder="Enter amount (min ₹10,000)"
              class="w-full px-4 sm:px-5 py-3 sm:py-4 border-2 border-gray-300 rounded-xl focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-200 text-base sm:text-lg transition"
            />
            <p class="text-xs sm:text-sm text-gray-500 mt-1 sm:mt-2">Minimum requested limit: ₹10,000</p>
          </div>

          <!-- Submit -->
          <div class="mt-10 sm:mt-12 flex justify-center md:justify-start">
            <button
              @click="submitApplication"
              :disabled="loading || !selectedCard || requestedLimit < 10000"
              class="bg-gradient-to-r from-purple-600 to-indigo-600 text-white px-8 sm:px-10 py-3 sm:py-4 rounded-full text-base sm:text-lg font-semibold shadow-lg hover:shadow-xl hover:scale-105 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 sm:gap-3 min-w-[220px] sm:min-w-[260px] justify-center touch-manipulation"
            >
              <span v-if="loading">Submitting...</span>
              <span v-else>Submit Application</span>
              <svg v-if="!loading" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

const cards = ref([
  { id: 1, cardType: 'VISA', title: 'Visa Signature', description: 'Global acceptance with premium rewards & travel benefits' },
  { id: 2, cardType: 'MASTERCARD', title: 'World Elite Mastercard', description: 'Exclusive perks, airport lounge access & high rewards' },
  { id: 3, cardType: 'AMEX', title: 'American Express Platinum', description: 'Premium service, concierge & luxury benefits' },
])

const selectedCard = ref(null)
const requestedLimit = ref(0)
const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Auto-redirect after success
watch(successMessage, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      router.push('/card')
    }, 3000)
  }
})

const getAuthToken = () => localStorage.getItem('authToken') || sessionStorage.getItem('authToken')

onMounted(() => {
  if (!getAuthToken()) {
    errorMessage.value = 'Please sign in to apply for a card.'
    setTimeout(() => router.push('/login'), 2000)
  }
})

const selectCard = (card) => {
  selectedCard.value = card
}

const resetForm = () => {
  selectedCard.value = null
  requestedLimit.value = 0
  successMessage.value = ''
  errorMessage.value = ''
}

const submitApplication = async () => {
  successMessage.value = ''
  errorMessage.value = ''

  const token = getAuthToken()
  if (!token) {
    errorMessage.value = 'Session expired. Please sign in again.'
    setTimeout(() => router.push('/login'), 2000)
    return
  }

  if (!selectedCard.value) {
    errorMessage.value = 'Please select a card type.'
    return
  }

  if (requestedLimit.value < 10000) {
    errorMessage.value = 'Requested limit must be at least ₹10,000.'
    return
  }

  loading.value = true

  try {
    const payload = {
      cardType: selectedCard.value.cardType,
      requestedLimit: requestedLimit.value
    }

    const response = await axios.post(
      `${API_BASE_URL}/cards/apply`,
      payload,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    )

    successMessage.value =
      response.data?.message ||
      response.data ||
      `Application for ${selectedCard.value.title} submitted successfully! Redirecting...`

    // IMPORTANT: resetForm() is NOT called here anymore → success screen stays visible

  } catch (error) {
    console.error('Application error:', error)

    if (error.response) {
      errorMessage.value = error.response.data?.message || error.response.data || 'Failed to submit application.'

      if (error.response.status === 401) {
        errorMessage.value = 'Session expired. Please sign in again.'
        setTimeout(() => {
          localStorage.removeItem('authToken')
          sessionStorage.removeItem('authToken')
          router.push('/login')
        }, 2000)
      }
    } else {
      errorMessage.value = 'Unable to connect. Please check your internet.'
    }
  } finally {
    loading.value = false
  }
}

const getCardBackground = (type) => {
  const t = (type || '').toUpperCase()
  if (t.includes('VISA')) return 'background: linear-gradient(135deg, #1e40af, #3b82f6)'
  if (t.includes('MASTER')) return 'background: linear-gradient(135deg, #dc2626, #f97316)'
  if (t.includes('AMEX')) return 'background: linear-gradient(135deg, #059669, #10b981)'
  return 'background: linear-gradient(135deg, #4f46e5, #7c3aed)'
}

const getCardLogoText = (type) => {
  const t = (type || '').toUpperCase()
  if (t.includes('VISA')) return 'VISA'
  if (t.includes('MASTER')) return 'Mastercard'
  if (t.includes('AMEX')) return 'AMEX'
  return 'CARD'
}
</script>

<style scoped>
.cursor-pointer:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

button {
  transition: all 0.2s ease;
}
</style>