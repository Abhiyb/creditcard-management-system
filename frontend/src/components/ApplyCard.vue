<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-50 to-blue-50 py-10 px-4 sm:px-6 lg:px-8">
    <div class="max-w-5xl mx-auto bg-white rounded-3xl shadow-xl overflow-hidden border border-gray-100">
      <!-- Header -->
      <div class="bg-gradient-to-r from-purple-600 to-indigo-600 p-8 md:p-10 text-white">
        <h2 class="text-3xl md:text-4xl font-bold tracking-tight">Apply for a New Credit Card</h2>
        <p class="text-lg mt-3 opacity-95">Choose your card and request your desired limit</p>
      </div>

      <!-- Main Content -->
      <div class="p-6 md:p-10 lg:p-12">
        <!-- Loading -->
        <div v-if="loading" class="flex flex-col items-center justify-center py-24">
          <div class="animate-spin rounded-full h-14 w-14 border-t-4 border-purple-600 border-b-4 border-purple-200 mb-5"></div>
          <p class="text-lg font-medium text-gray-700">Submitting your application... Please wait</p>
        </div>

        <!-- Error -->
        <div v-else-if="errorMessage" class="text-center py-16">
          <div class="bg-red-50 border border-red-300 text-red-800 px-6 py-10 rounded-2xl max-w-2xl mx-auto shadow-sm">
            <svg class="w-16 h-16 text-red-500 mx-auto mb-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <p class="text-2xl font-bold mb-4">Something went wrong</p>
            <p class="text-lg mb-6">{{ errorMessage }}</p>
            <button
              @click="resetForm"
              class="bg-red-600 hover:bg-red-700 text-white px-8 py-3 rounded-xl font-medium transition shadow-md"
            >
              Try Again
            </button>
          </div>
        </div>

        <!-- Success + Auto Redirect -->
        <div v-else-if="successMessage" class="text-center py-20">
          <div class="bg-green-50 border border-green-200 text-green-800 px-6 py-12 rounded-2xl max-w-2xl mx-auto shadow-sm">
            <svg class="w-20 h-20 text-green-500 mx-auto mb-6" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586l-1.293-1.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <h3 class="text-3xl font-bold mb-4">Application Submitted Successfully!</h3>
            <p class="text-xl mb-6">{{ successMessage }}</p>
            <p class="text-gray-600 text-lg">
              Redirecting to your applications page in <strong>{{ countdown }}</strong> seconds...
            </p>
          </div>
        </div>

        <!-- Form -->
        <div v-else>
          <h3 class="text-2xl md:text-3xl font-semibold text-gray-900 mb-8 text-center">
            Select Your Preferred Card
          </h3>

          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8 mb-12">
            <div
              v-for="card in cards"
              :key="card.id"
              @click="selectCard(card)"
              class="cursor-pointer rounded-2xl overflow-hidden border-2 transition-all duration-300 hover:shadow-2xl hover:scale-[1.03] active:scale-[0.98]"
              :class="{
                'border-purple-500 ring-2 ring-purple-300 bg-purple-50/40': selectedCard?.id === card.id,
                'border-gray-200 hover:border-purple-400': selectedCard?.id !== card.id
              }"
            >
              <!-- Card Preview -->
              <div
                :style="getCardBackground(card.cardType)"
                class="h-52 sm:h-56 p-6 text-white relative"
              >
                <div class="flex justify-between items-start">
                  <span class="text-3xl font-extrabold tracking-wide drop-shadow-lg">
                    {{ getCardLogoText(card.cardType) }}
                  </span>
                  <div class="w-14 h-10 bg-white/20 backdrop-blur-sm rounded-lg flex items-center justify-center text-xs font-bold shadow-inner border border-white/30">
                    CHIP
                  </div>
                </div>

                <div class="absolute bottom-6 left-6 right-6">
                  <p class="text-2xl font-mono tracking-widest drop-shadow-lg mb-4">
                    **** **** **** 4242
                  </p>
                  <div class="flex justify-between text-sm">
                    <div>
                      <div class="text-xs opacity-80 uppercase">Valid Thru</div>
                      <div class="font-medium">12/29</div>
                    </div>
                    <div class="text-xl font-bold uppercase tracking-wider opacity-95">
                      {{ getCardLogoText(card.cardType) }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- Details -->
              <div class="p-6 bg-white">
                <h4 class="font-bold text-xl text-gray-900 mb-2">{{ card.title }}</h4>
                <p class="text-gray-600 text-sm leading-relaxed">{{ card.description }}</p>
              </div>
            </div>
          </div>

          <!-- Limit Input -->
          <div class="max-w-lg mx-auto">
            <label class="block text-gray-800 font-semibold text-lg mb-3">
              Requested Credit Limit (₹)
            </label>
            <input
              v-model.number="requestedLimit"
              type="number"
              min="10000"
              step="1000"
              placeholder="e.g. 50000"
              :class="[
                'w-full px-6 py-4 border-2 rounded-xl text-lg transition focus:outline-none focus:ring-2 focus:ring-purple-300',
                requestedLimit && requestedLimit < 10000 ? 'border-red-500 bg-red-50' : 'border-gray-300 focus:border-purple-500'
              ]"
            />
            <p
              v-if="requestedLimit && requestedLimit < 10000"
              class="mt-2 text-red-600 text-sm font-medium"
            >
              Minimum limit is ₹10,000
            </p>
            <p class="mt-2 text-gray-500 text-sm">
              Minimum requested limit: ₹10,000 (amount in whole numbers)
            </p>
          </div>

          <!-- Submit -->
          <div class="mt-12 text-center">
            <button
              @click="submitApplication"
              :disabled="loading || !selectedCard || !requestedLimit || requestedLimit < 10000"
              class="bg-gradient-to-r from-purple-600 to-indigo-600 text-white px-12 py-5 rounded-full text-xl font-semibold shadow-xl hover:shadow-2xl hover:scale-105 transition-all duration-300 disabled:opacity-60 disabled:hover:scale-100 disabled:cursor-not-allowed flex items-center justify-center gap-3 mx-auto"
              title="Please select card and enter valid limit ≥ ₹10,000"
            >
              <span v-if="loading">Processing...</span>
              <span v-else>Submit Application</span>
              <svg v-if="!loading" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

const cards = ref([
  {
    id: 1,
    cardType: 'VISA',
    title: 'Visa Signature',
    description: 'Widely accepted worldwide • Premium rewards • Travel insurance benefits'
  },
  {
    id: 2,
    cardType: 'MASTERCARD',
    title: 'World Elite Mastercard',
    description: 'Luxury perks • Airport lounge access • High reward rates'
  },
  {
    id: 3,
    cardType: 'AMEX',
    title: 'American Express Platinum',
    description: 'Elite concierge • Luxury benefits • Exclusive experiences'
  }
])

const selectedCard = ref(null)
const requestedLimit = ref(null)
const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const countdown = ref(3)

const getAuthToken = () => localStorage.getItem('authToken') || sessionStorage.getItem('authToken')

watch(successMessage, (val) => {
  if (val) {
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        router.push('/card')  // or '/applications', '/my-cards' — adjust as needed
      }
    }, 1000)
  }
})

const selectCard = (card) => {
  selectedCard.value = card
}

const resetForm = () => {
  selectedCard.value = null
  requestedLimit.value = null
  successMessage.value = ''
  errorMessage.value = ''
  countdown.value = 3
}

const submitApplication = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  const token = getAuthToken()
  if (!token) {
    errorMessage.value = 'Please sign in to continue.'
    setTimeout(() => router.push('/login'), 2200)
    return
  }

  if (!selectedCard.value) {
    errorMessage.value = 'Please select a card type.'
    return
  }

  if (!requestedLimit.value || requestedLimit.value < 10000) {
    errorMessage.value = 'Requested limit must be at least ₹10,000.'
    return
  }

  loading.value = true

  try {
    const payload = {
      cardType: selectedCard.value.cardType,
      requestedLimit: Number(requestedLimit.value)
    }

    const response = await axios.post(
      `${API_BASE_URL}/cards/apply`,
      payload,
      { headers: { Authorization: `Bearer ${token}` } }
    )

    successMessage.value = response.data?.message ||
      `Your application for the ${selectedCard.value.title} has been successfully submitted. We'll review it shortly!`
  } catch (err) {
    console.error(err)
    if (err.response) {
      errorMessage.value = err.response.data?.message || 'Failed to submit application.'
      if (err.response.status === 401) {
        errorMessage.value = 'Session expired. Redirecting to login...'
        setTimeout(() => {
          localStorage.removeItem('authToken')
          sessionStorage.removeItem('authToken')
          router.push('/login')
        }, 2500)
      }
    } else {
      errorMessage.value = 'Network error. Please check your connection and try again.'
    }
  } finally {
    loading.value = false
  }
}

const getCardBackground = (type) => {
  const t = (type || '').toUpperCase()
  if (t.includes('VISA'))     return 'background: linear-gradient(135deg, #1e3a8a, #3b82f6)'
  if (t.includes('MASTER'))   return 'background: linear-gradient(135deg, #b91c1c, #f97316)'
  if (t.includes('AMEX'))     return 'background: linear-gradient(135deg, #047857, #10b981)'
  return 'background: linear-gradient(135deg, #6d28d9, #a855f7)'
}

const getCardLogoText = (type) => {
  const t = (type || '').toUpperCase()
  if (t.includes('VISA'))     return 'VISA'
  if (t.includes('MASTER'))   return 'Mastercard'
  if (t.includes('AMEX'))     return 'AMEX'
  return 'CARD'
}
</script>

<style scoped>
/* Smooth hover effects already in classes */
</style>