<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-50 to-blue-50 py-10 px-4">
    <div class="max-w-5xl mx-auto bg-white rounded-3xl shadow-xl overflow-hidden">
      <!-- Header -->
      <div class="bg-gradient-to-r from-purple-600 to-indigo-600 p-8 text-white">
        <h2 class="text-3xl md:text-4xl font-bold">Apply for a New Card</h2>
        <p class="text-lg mt-2 opacity-90">Step 1: Choose your perfect card</p>
      </div>

      <!-- Main Form Area -->
      <div class="p-8 md:p-12">
        <!-- Loading -->
        <div v-if="loading" class="flex justify-center items-center py-20">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div>
          <span class="ml-4 text-gray-700 font-medium">Submitting your application...</span>
        </div>

        <!-- Error -->
        <div v-else-if="errorMessage" class="text-center py-12">
          <div class="bg-red-50 border border-red-200 text-red-700 px-6 py-4 rounded-xl max-w-2xl mx-auto">
            <p class="text-lg font-semibold">Oops!</p>
            <p class="mt-2">{{ errorMessage }}</p>
            <button @click="resetForm" class="mt-6 bg-red-600 hover:bg-red-700 text-white px-6 py-2 rounded-lg transition">
              Try Again
            </button>
          </div>
        </div>

        <!-- SUCCESS SCREEN (now guaranteed to show) -->
        <div v-else-if="successMessage" class="text-center py-16">
          <div class="bg-green-50 border border-green-300 text-green-800 px-8 py-12 rounded-2xl max-w-2xl mx-auto shadow-lg">
            <svg class="w-20 h-20 text-green-500 mx-auto mb-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p class="text-3xl font-bold mb-4">Application Submitted!</p>
            <p class="text-xl mb-8">{{ successMessage }}</p>
            <p class="text-gray-600 text-lg">Redirecting to <strong>My Cards</strong> in 3 seconds...</p>
          </div>
        </div>

        <!-- Card Selection & Form -->
        <div v-else>
          <h3 class="text-2xl font-semibold text-gray-800 mb-6 text-center md:text-left">
            Select Your Card Type
          </h3>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
            <div
              v-for="card in cards"
              :key="card.id"
              @click="selectCard(card)"
              :class="[
                'cursor-pointer rounded-2xl overflow-hidden border-2 transition-all duration-300 hover:shadow-xl hover:scale-[1.02]',
                selectedCard?.id === card.id
                  ? 'border-purple-500 bg-purple-50/50 ring-2 ring-purple-300'
                  : 'border-gray-200 hover:border-purple-300'
              ]"
            >
              <div :style="getCardBackground(card.cardType)" class="h-48 px-6 py-6 text-white relative rounded-t-2xl">
                <div class="flex justify-between items-start">
                  <span class="text-2xl font-bold tracking-wider drop-shadow-md">
                    {{ getCardLogoText(card.cardType) }}
                  </span>
                  <div class="w-12 h-9 bg-yellow-400 rounded-md flex items-center justify-center text-black font-bold text-xs shadow-md">
                    CHIP
                  </div>
                </div>
                <div class="mt-8">
                  <p class="text-2xl font-mono tracking-widest drop-shadow-md">
                    **** **** **** 1234
                  </p>
                </div>
                <div class="mt-6 flex justify-between text-sm drop-shadow-md">
                  <div>
                    <div class="text-xs opacity-80">VALID THRU</div>
                    <div class="text-base">12/30</div>
                  </div>
                  <div class="text-xl font-bold uppercase tracking-wider opacity-90">
                    {{ getCardLogoText(card.cardType) }}
                  </div>
                </div>
              </div>

              <div class="p-6 bg-white rounded-b-2xl">
                <h4 class="font-bold text-lg text-gray-800 mb-2">{{ card.title }}</h4>
                <p class="text-sm text-gray-600">{{ card.description }}</p>
              </div>
            </div>
          </div>

          <!-- Credit Limit -->
          <div class="max-w-md mx-auto md:mx-0">
            <label class="block text-gray-700 font-medium mb-3 text-lg">
              Requested Credit Limit (₹)
            </label>
            <input
              v-model.number="requestedLimit"
              type="number"
              min="10000"
              step="1000"
              placeholder="Enter amount (min ₹10,000)"
              class="w-full px-5 py-4 border-2 border-gray-300 rounded-xl focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-200 text-lg transition"
            />
            <p class="text-sm text-gray-500 mt-2">Minimum requested limit: ₹10,000</p>
          </div>

          <!-- Submit Button -->
          <div class="mt-12 flex justify-center md:justify-start">
            <button
              @click="submitApplication"
              :disabled="loading || !selectedCard || !requestedLimit || requestedLimit < 10000"
              class="bg-gradient-to-r from-purple-600 to-indigo-600 text-white px-10 py-4 rounded-full text-lg font-semibold shadow-lg hover:shadow-xl hover:scale-105 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-3"
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
const requestedLimit = ref(null)
const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// ====================== REDIRECT + DEBUG ======================
watch(successMessage, (newVal) => {
  if (newVal) {
    console.log('🔄 Success message detected → redirecting to /card in 3.4 seconds')
    setTimeout(() => {
      console.log('🚀 Redirecting to /card now')
      router.push('/card')
    }, 3400)
  }
})

// ====================== AUTH ======================
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
  requestedLimit.value = null
  successMessage.value = ''
  errorMessage.value = ''
  loading.value = false
}

// ====================== SUBMIT (NOW BULLETPROOF) ======================
const submitApplication = async () => {
  console.log('🚀 Submit button clicked')

  successMessage.value = ''
  errorMessage.value = ''

  const token = getAuthToken()
  if (!token) {
    errorMessage.value = 'Session expired. Please sign in again.'
    console.log('❌ No token')
    return
  }

  if (!selectedCard.value) {
    errorMessage.value = 'Please select a card type.'
    console.log('❌ No card selected')
    return
  }
  if (!requestedLimit.value || requestedLimit.value < 10000) {
    errorMessage.value = 'Requested limit must be at least ₹10,000.'
    console.log('❌ Invalid limit')
    return
  }

  loading.value = true

  try {
    const payload = {
      cardType: selectedCard.value.cardType,
      requestedLimit: Number(requestedLimit.value)
    }

    console.log('📤 Sending payload:', payload)

    const response = await axios.post(
      `${API_BASE_URL}/cards/apply`,
      payload,
      { headers: { Authorization: `Bearer ${token}` } }
    )

    console.log('✅ API Response received:', response.data)

    // FORCE a proper string message (this was the hidden bug)
    let displayMessage = ''
    if (response.data?.message && typeof response.data.message === 'string') {
      displayMessage = response.data.message
    } else if (response.data && typeof response.data === 'string') {
      displayMessage = response.data
    } else {
      displayMessage = `Your application for ${selectedCard.value.title} has been submitted successfully!`
    }

    successMessage.value = displayMessage
    console.log('✅ SUCCESS MESSAGE SET TO:', successMessage.value)

    // No resetForm() here — this was the old bug

  } catch (error) {
    console.error('❌ Full error object:', error)
    console.log('❌ Response data (if any):', error.response?.data)

    if (error.response) {
      errorMessage.value = error.response.data?.message || error.response.data || 'Failed to submit application.'
    } else {
      errorMessage.value = 'Unable to connect. Please check your internet.'
    }
  } finally {
    loading.value = false
    console.log('🏁 Request finished (loading = false)')
  }
}

// Card styling (unchanged)
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
</style>