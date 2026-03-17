<template>
  <div class="min-h-screen bg-gray-50 p-4 md:p-8 font-sans">
    <div class="max-w-4xl mx-auto bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100">
      <!-- Header -->
      <div class="bg-gradient-to-r from-blue-700 to-indigo-700 text-white p-6 flex justify-between items-center">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-white/20 rounded-2xl flex items-center justify-center text-2xl">💳</div>
          <div>
            <h1 class="text-2xl font-bold tracking-tight">BNPL Payments</h1>
            <p class="text-sm opacity-90">Secure • Flexible • Instant</p>
          </div>
        </div>
        <button @click="$router.push('/dashboard')" class="flex items-center gap-2 px-5 py-2.5 bg-white text-blue-700 rounded-2xl font-medium hover:bg-gray-100">
          ← Dashboard
        </button>
      </div>

      <!-- Progress - only for payment flow -->
      <div v-if="currentStep < 4" class="px-8 pt-8 pb-4">
        <div class="flex items-center justify-between">
          <div v-for="(stepName, index) in steps" :key="index" class="flex flex-col items-center flex-1 relative">
            <div class="w-9 h-9 rounded-full flex items-center justify-center text-sm font-semibold" :class="currentStep >= index ? 'bg-blue-700 text-white' : 'bg-gray-200 text-gray-500'">
              {{ index + 1 }}
            </div>
            <div class="text-xs mt-2" :class="currentStep >= index ? 'text-blue-700' : 'text-gray-500'">
              {{ stepName }}
            </div>
          </div>
        </div>
      </div>

      <div class="p-8">
        <div v-if="errorMessage" class="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded-xl flex items-center gap-3">
          <span class="text-xl">⚠️</span> {{ errorMessage }}
        </div>
        <div v-if="successMessage" class="mb-6 p-4 bg-green-50 border border-green-200 text-green-700 rounded-xl flex items-center gap-3">
          <span class="text-xl">✅</span> {{ successMessage }}
        </div>

        <!-- STEP 0: New Payment -->
        <div v-if="currentStep === 0" class="space-y-8">
          <h2 class="text-2xl font-semibold">New Payment</h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="md:col-span-2 space-y-2">
              <label class="block text-sm font-medium">Card Number <span class="text-red-600">*</span></label>
              <input v-model="transaction.cardNumber" maxlength="19" class="w-full px-4 py-3 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="1234 5678 9012 3456" @input="formatCardNumber" />
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium">CVV <span class="text-red-600">*</span></label>
              <input v-model="transaction.cvv" type="password" maxlength="4" class="w-full px-4 py-3 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="123" />
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium">Expiry (MM/YY) <span class="text-red-600">*</span></label>
              <div class="flex gap-2">
                <input v-model="transaction.expiryMonth" maxlength="2" class="w-20 px-4 py-3 border rounded-xl text-center" placeholder="MM" />
                <span class="self-center text-gray-400">/</span>
                <input v-model="transaction.expiryYear" maxlength="2" class="w-20 px-4 py-3 border rounded-xl text-center" placeholder="YY" />
              </div>
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium">Amount (₹) <span class="text-red-600">*</span></label>
              <input v-model.number="transaction.amount" type="number" min="1" class="w-full px-4 py-3 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="1000" />
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium">Category <span class="text-red-600">*</span></label>
              <select v-model="transaction.category" class="w-full px-4 py-3 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="" disabled>Select category</option>
                <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
              </select>
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium">Merchant <span class="text-red-600">*</span></label>
              <select v-model="transaction.merchantName" class="w-full px-4 py-3 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="" disabled>Select merchant</option>
                <option v-for="mer in merchants" :key="mer" :value="mer">{{ mer }}</option>
              </select>
            </div>
          </div>

          <div class="flex flex-col sm:flex-row gap-4 pt-6">
            <button @click="checkEligibility" :disabled="loading || !isFormValid" class="flex-1 py-3.5 bg-blue-700 text-white rounded-xl font-medium hover:bg-blue-800 disabled:opacity-60">
              {{ loading ? 'Checking...' : 'Continue to Payment' }}
            </button>
            <button @click="viewTransactionHistory" class="px-7 py-3.5 border border-gray-300 rounded-xl hover:bg-gray-50">View Transactions</button>
            <button @click="goToAllInstallments" class="px-7 py-3.5 border border-gray-300 rounded-xl hover:bg-gray-50">View Installments</button>
          </div>
        </div>

        <!-- STEP 1: Payment Options (unchanged) -->
        <div v-if="currentStep === 1" class="space-y-8">
          <!-- ... your original Step 1 code here (I omitted for brevity but keep it as it was working) ... -->
        </div>

        <!-- STEP 2: Confirmation (unchanged) -->
        <div v-if="currentStep === 2" class="space-y-8">
          <!-- ... your original Step 2 code ... -->
        </div>

        <!-- STEP 3: Complete (unchanged) -->
        <div v-if="currentStep === 3" class="py-12 text-center space-y-8">
          <!-- ... your original Step 3 code ... -->
        </div>

        <!-- VIEW 5: All Transactions -->
        <div v-if="currentStep === 5" class="space-y-8">
          <h2 class="text-2xl font-semibold">All Transactions</h2>

          <div class="flex flex-wrap gap-4 items-center">
            <div class="flex gap-2">
              <button v-for="f in ['all','full','bnpl']" :key="f" @click="filterTransactions(f)" class="px-5 py-2 text-sm rounded-xl" :class="transactionFilter === f ? 'bg-blue-700 text-white' : 'bg-gray-100 hover:bg-gray-200'">
                {{ f === 'all' ? 'All' : f === 'full' ? 'Full' : 'BNPL' }}
              </button>
            </div>
            <select v-model="sortOption" class="px-5 py-2.5 border rounded-xl">
              <option value="newest">Newest first</option>
              <option value="oldest">Oldest first</option>
              <option value="amount-desc">Highest amount</option>
              <option value="amount-asc">Lowest amount</option>
            </select>
          </div>

          <div class="flex flex-col sm:flex-row gap-4">
            <div class="flex-1">
              <label class="block text-sm mb-1">From</label>
              <input v-model="dateFilter.from" type="date" class="w-full px-4 py-2.5 border rounded-xl" />
            </div>
            <div class="flex-1">
              <label class="block text-sm mb-1">To</label>
              <input v-model="dateFilter.to" type="date" class="w-full px-4 py-2.5 border rounded-xl" />
            </div>
          </div>

          <div v-if="loading" class="text-center py-12">Loading...</div>
          <div v-else-if="!paginatedTransactions.length" class="text-center py-12 text-gray-500">No transactions</div>
          <div v-else>
            <div class="overflow-x-auto rounded-xl border">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
                    <th class="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase">Card</th>
                    <th class="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase">Merchant</th>
                    <th class="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
                    <th class="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-200">
                  <tr v-for="t in paginatedTransactions" :key="t.id" @click="selectedTransaction = t" class="hover:bg-gray-50 cursor-pointer">
                    <td class="px-6 py-4 text-sm">{{ formatDate(t.transactionDate) }}</td>
                    <td class="px-6 py-4 font-mono text-sm">•••• {{ (t.cardNumber || 'XXXX').slice(-4) }}</td>
                    <td class="px-6 py-4 text-sm">{{ t.merchantName || '—' }}</td>
                    <td class="px-6 py-4 font-medium">₹{{ Number(t.amount).toLocaleString('en-IN') }}</td>
                    <td class="px-6 py-4">
                      <span class="inline-flex px-3 py-1 text-xs rounded-full" :class="t.isBNPL ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'">
                        {{ t.isBNPL ? 'BNPL' : 'Full' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="selectedTransaction" class="mt-6 p-6 bg-gray-50 rounded-2xl border">
              <div class="flex justify-between mb-4">
                <h3 class="text-lg font-semibold">Transaction #{{ selectedTransaction.id }}</h3>
                <button @click="selectedTransaction = null" class="text-gray-500 hover:text-gray-700">Close</button>
              </div>
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div><strong>Date:</strong> {{ formatDate(selectedTransaction.transactionDate) }}</div>
                <div><strong>Card:</strong> •••• {{ (selectedTransaction.cardNumber || 'XXXX').slice(-4) }}</div>
                <div><strong>Merchant:</strong> {{ selectedTransaction.merchantName || '—' }}</div>
                <div><strong>Amount:</strong> ₹{{ Number(selectedTransaction.amount).toLocaleString('en-IN') }}</div>
                <div><strong>Type:</strong> {{ selectedTransaction.isBNPL ? 'BNPL' : 'Full Payment' }}</div>
              </div>
            </div>

            <div v-if="totalPages > 1" class="flex justify-between items-center mt-6">
              <button @click="prevPage" :disabled="currentPage === 1" class="px-6 py-2 border rounded-xl disabled:opacity-40">Previous</button>
              <span>Page {{ currentPage }} of {{ totalPages }}</span>
              <button @click="nextPage" :disabled="currentPage === totalPages" class="px-6 py-2 border rounded-xl disabled:opacity-40">Next</button>
            </div>
          </div>

          <button @click="currentStep = 0" class="mt-8 w-full py-4 border rounded-xl hover:bg-gray-50">Back to New Payment</button>
        </div>

        <!-- VIEW 6: All Installments -->
        <div v-if="currentStep === 6" class="space-y-8">
          <h2 class="text-2xl font-semibold">All Installments</h2>

          <div class="flex gap-2">
            <button v-for="f in ['all','pending','paid','overdue']" :key="f" @click="installmentFilter = f" class="px-5 py-2 text-sm rounded-xl" :class="installmentFilter === f ? 'bg-blue-700 text-white' : 'bg-gray-100 hover:bg-gray-200'">
              {{ f.charAt(0).toUpperCase() + f.slice(1) }}
            </button>
          </div>

          <div v-if="!filteredInstallments.length" class="text-center py-12 text-gray-500">No installments found</div>
          <div v-else class="space-y-6">
            <div v-for="group in groupedInstallments" :key="group[0]?.transactionId" class="border rounded-2xl overflow-hidden">
              <div class="bg-gray-50 px-6 py-4 font-medium flex justify-between">
                <span>Transaction #{{ group[0]?.transactionId || '—' }}</span>
                <span>Total: ₹{{ group.reduce((s, i) => s + Number(i.amount), 0).toFixed(2) }}</span>
              </div>
              <table class="min-w-full">
                <thead class="bg-white">
                  <tr class="border-b">
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500">ID</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500">Due Date</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500">Amount</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500">Status</th>
                    <th class="px-6 py-3 text-right text-xs font-medium text-gray-500">Action</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="inst in group" :key="inst.id" class="hover:bg-gray-50">
                    <td class="px-6 py-4 text-sm">{{ inst.id }}</td>
                    <td class="px-6 py-4 text-sm">{{ formatDate(inst.dueDate) }}</td>
                    <td class="px-6 py-4 font-medium">₹{{ Number(inst.amount).toFixed(2) }}</td>
                    <td class="px-6 py-4">
                      <span class="px-3 py-1 text-xs rounded-full" :class="inst.isPaid ? 'bg-green-100 text-green-800' : isOverdue(inst.dueDate) ? 'bg-red-100 text-red-800' : 'bg-amber-100 text-amber-800'">
                        {{ inst.isPaid ? 'Paid' : isOverdue(inst.dueDate) ? 'Overdue' : 'Pending' }}
                      </span>
                    </td>
                    <td class="px-6 py-4 text-right">
                      <button v-if="!inst.isPaid" @click="payInstallment(inst)" class="px-4 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50" :disabled="loading">
                        Pay Now
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <button @click="currentStep = 0" class="w-full py-4 border rounded-xl hover:bg-gray-50">Back to New Payment</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000'

const getAuthToken = () => {
  // Priority: localStorage → cookie fallback
  let token = localStorage.getItem('authToken') || localStorage.getItem('token')
  if (!token) {
    const match = document.cookie.match(/authToken=([^;]+)/)
    if (match) token = match[1]
  }
  return token || ''
}

const steps = ['Details', 'Options', 'Confirm', 'Complete']

const currentStep = ref(0)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const transaction = ref({
  cardNumber: '', cvv: '', expiryMonth: '', expiryYear: '', amount: '', category: '', merchantName: '', isBNPL: false
})

const categories = ref(['Electronics', 'Clothing', 'Travel', 'Home & Kitchen', 'Food', 'Entertainment', 'Other'])
const merchants = ref(['Amazon', 'Flipkart', 'Myntra', 'Nykaa', 'Swiggy', 'Zomato', 'Other'])

const planMapping = { 3: 'THREE', 6: 'SIX', 9: 'NINE' }

const eligibilityResult = ref({ eligible: false, message: '' })
const selectedPaymentMethod = ref(null)
const selectedPlan = ref(null)
const installmentPlans = ref([
  { months: 3, interestRate: 0 },
  { months: 6, interestRate: 2.5 },
  { months: 9, interestRate: 4 }
])

const transactions = ref([])
const transactionFilter = ref('all')
const dateFilter = ref({ from: '', to: '' })
const sortOption = ref('newest')
const currentPage = ref(1)
const pageSize = 10

const selectedTransaction = ref(null)

const displayedInstallments = ref([])
const installmentFilter = ref('all')
const confirmingInstallment = ref(null)

// ─── Computed ────────────────────────────────────────────────────────────────
const isFormValid = computed(() => {
  const clean = transaction.value.cardNumber.replace(/\s/g, '')
  return clean.length === 16 && /^\d+$/.test(clean) &&
         transaction.value.cvv?.length >= 3 &&
         transaction.value.expiryMonth?.match(/^(0[1-9]|1[0-2])$/) &&
         transaction.value.expiryYear?.match(/^\d{2}$/) &&
         Number(transaction.value.amount) > 0 &&
         transaction.value.category && transaction.value.merchantName
})

const canProceed = computed(() => selectedPaymentMethod.value === 'full' || (selectedPaymentMethod.value === 'bnpl' && selectedPlan.value))

const filteredTransactions = computed(() => {
  let list = transactions.value.slice()

  // Correct filter logic
  if (transactionFilter.value === 'full') list = list.filter(t => !t.isBNPL)
  if (transactionFilter.value === 'bnpl') list = list.filter(t => !!t.isBNPL)

  if (dateFilter.value.from) list = list.filter(t => new Date(t.transactionDate) >= new Date(dateFilter.value.from))
  if (dateFilter.value.to) list = list.filter(t => new Date(t.transactionDate) <= new Date(dateFilter.value.to))

  // Sorting - newest first fixed
  list.sort((a, b) => {
    const da = new Date(a.transactionDate)
    const db = new Date(b.transactionDate)
    if (sortOption.value === 'newest') return db - da
    if (sortOption.value === 'oldest') return da - db
    if (sortOption.value === 'amount-desc') return Number(b.amount) - Number(a.amount)
    if (sortOption.value === 'amount-asc') return Number(a.amount) - Number(b.amount)
    return 0
  })

  return list
})

const paginatedTransactions = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredTransactions.value.slice(start, start + pageSize)
})

const totalPages = computed(() => Math.ceil(filteredTransactions.value.length / pageSize))

// ─── API Calls ───────────────────────────────────────────────────────────────
const checkEligibility = async () => {
  if (!isFormValid.value) {
    errorMessage.value = 'Please fill all required fields correctly'
    return
  }
  loading.value = true
  errorMessage.value = ''
  const token = getAuthToken()
  if (!token) {
    errorMessage.value = 'Please login again'
    router.push('/login')
    return
  }
  try {
    const payload = {
      cardNumber: transaction.value.cardNumber.replace(/\s/g, ''),
      cvv: transaction.value.cvv,
      expiryMonth: transaction.value.expiryMonth,
      expiryYear: transaction.value.expiryYear,
      amount: Number(transaction.value.amount),
      category: transaction.value.category,
      merchantName: transaction.value.merchantName
    }
    const res = await fetch(`${API_BASE_URL}/transactions/validate-card`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    })

    if (res.status === 401) {
      localStorage.removeItem('authToken')
      localStorage.removeItem('token')
      router.push('/login')
      return
    }

    if (!res.ok) throw new Error('Validation failed')
    const data = await res.json()
    eligibilityResult.value = { eligible: !!data.eligible, message: data.message || '' }
    currentStep.value = 1
  } catch (err) {
    errorMessage.value = err.message || 'Could not validate card'
  } finally {
    loading.value = false
  }
}

// ... keep your confirmTransaction, fetchTransactions, payInstallment, etc. the same as in the version that was working

const viewTransactionHistory = () => {
  currentStep.value = 5
  fetchTransactions()
}

const goToAllInstallments = () => {
  currentStep.value = 6
  // fetchAllInstallments() if you have the endpoint
}

const resetForm = () => {
  currentStep.value = 0
  transaction.value = { cardNumber: '', cvv: '', expiryMonth: '', expiryYear: '', amount: '', category: '', merchantName: '', isBNPL: false }
  selectedPaymentMethod.value = null
  selectedPlan.value = null
  errorMessage.value = ''
  successMessage.value = ''
}

onMounted(() => {
  // Optional: check token validity on mount
  if (!getAuthToken()) router.push('/login')
})
</script>