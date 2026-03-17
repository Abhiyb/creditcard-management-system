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
        <button @click="$router.push('/dashboard')" class="flex items-center gap-2 px-5 py-2.5 bg-white text-blue-700 rounded-2xl font-medium hover:bg-gray-100 transition-all">
          ← Dashboard
        </button>
      </div>

      <!-- Progress only for payment flow -->
      <div v-if="currentStep < 4" class="px-8 pt-8 pb-4">
        <div class="flex items-center justify-between">
          <div v-for="(stepName, index) in paymentSteps" :key="index" class="flex flex-col items-center flex-1 relative">
            <div class="w-9 h-9 rounded-full flex items-center justify-center text-sm font-semibold" :class="currentStep >= index ? 'bg-blue-700 text-white' : 'bg-gray-200 text-gray-500'">
              {{ index + 1 }}
            </div>
            <div class="text-xs mt-2 text-center" :class="currentStep >= index ? 'text-blue-700' : 'text-gray-500'">
              {{ stepName }}
            </div>
          </div>
        </div>
      </div>

      <div class="p-8">
        <!-- Messages -->
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
                <input v-model="transaction.expiryMonth" maxlength="2" class="w-20 px-4 py-3 border rounded-xl text-center focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="MM" />
                <span class="self-center text-gray-400">/</span>
                <input v-model="transaction.expiryYear" maxlength="2" class="w-20 px-4 py-3 border rounded-xl text-center focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="YY" />
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
            <button @click="viewTransactionHistory" class="py-3.5 px-6 border border-gray-300 rounded-xl hover:bg-gray-50">View Transactions</button>
            <button @click="goToAllInstallments" class="py-3.5 px-6 border border-gray-300 rounded-xl hover:bg-gray-50">View Installments</button>
          </div>
        </div>

        <!-- STEP 1: Payment Options -->
        <div v-if="currentStep === 1" class="space-y-8">
          <h2 class="text-2xl font-semibold">Payment Options</h2>

          <div v-if="eligibilityResult.eligible" class="space-y-6">
            <div class="p-6 bg-green-50 border border-green-200 rounded-2xl">
              <h3 class="font-semibold text-green-800">Eligible for BNPL</h3>
              <p class="text-green-700 mt-1">Split into 3, 6 or 9 months</p>
            </div>

            <div class="grid md:grid-cols-2 gap-6">
              <div @click="selectPaymentMethod('full')" class="p-6 border-2 rounded-2xl cursor-pointer transition-all" :class="selectedPaymentMethod === 'full' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-blue-300'">
                <div class="font-semibold text-lg">Pay in Full</div>
                <div class="text-2xl font-bold mt-4">₹{{ Number(transaction.amount).toLocaleString('en-IN') }}</div>
              </div>

              <div @click="selectPaymentMethod('bnpl')" class="p-6 border-2 rounded-2xl cursor-pointer transition-all" :class="selectedPaymentMethod === 'bnpl' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-blue-300'">
                <div class="font-semibold text-lg">Buy Now Pay Later</div>
                <div class="text-blue-600 mt-2">Flexible installments</div>
              </div>
            </div>

            <div v-if="selectedPaymentMethod === 'bnpl'" class="space-y-6">
              <h3 class="font-medium text-lg">Choose Plan</h3>
              <div class="grid md:grid-cols-3 gap-4">
                <div v-for="plan in installmentPlans" :key="plan.months" @click="selectInstallmentPlan(plan)" class="p-6 border-2 rounded-2xl cursor-pointer transition-all text-center" :class="selectedPlan?.months === plan.months ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-blue-300'">
                  <div class="text-3xl font-bold">{{ plan.months }}</div>
                  <div class="text-sm text-gray-600 mt-1">months</div>
                  <div class="text-xl font-semibold mt-3">₹{{ (transaction.amount / plan.months).toFixed(0) }}</div>
                  <div class="text-xs mt-1" :class="plan.interestRate === 0 ? 'text-green-600' : 'text-amber-600'">
                    {{ plan.interestRate === 0 ? '0% interest' : `${plan.interestRate}% interest` }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="p-8 bg-amber-50 border border-amber-200 rounded-2xl text-center">
            <h3 class="font-semibold text-amber-800">Not eligible for installments</h3>
            <p class="text-amber-700 mt-2">{{ eligibilityResult.message || 'Please pay in full' }}</p>
          </div>

          <div class="flex justify-between pt-6">
            <button @click="currentStep = 0" class="px-8 py-3 border rounded-xl hover:bg-gray-50">Back</button>
            <button @click="proceedToConfirmation" :disabled="!canProceed || loading" class="px-10 py-3 bg-blue-700 text-white rounded-xl hover:bg-blue-800 disabled:opacity-60">
              {{ loading ? 'Processing...' : 'Confirm Payment' }}
            </button>
          </div>
        </div>

        <!-- STEP 2: Confirmation -->
        <div v-if="currentStep === 2" class="space-y-8">
          <h2 class="text-2xl font-semibold">Review & Confirm</h2>

          <div class="bg-gray-50 rounded-2xl p-6 space-y-4">
            <div class="flex justify-between py-3 border-b">
              <span class="text-gray-600">Card ending</span>
              <span class="font-medium">•••• {{ transaction.cardNumber.slice(-4) || 'XXXX' }}</span>
            </div>
            <div class="flex justify-between py-3 border-b">
              <span class="text-gray-600">Amount</span>
              <span class="font-bold text-xl">₹{{ Number(transaction.amount).toLocaleString('en-IN') }}</span>
            </div>
            <div class="flex justify-between py-3 border-b">
              <span class="text-gray-600">Merchant</span>
              <span>{{ transaction.merchantName }}</span>
            </div>
            <div class="flex justify-between py-3 border-b">
              <span class="text-gray-600">Category</span>
              <span>{{ transaction.category }}</span>
            </div>
            <div v-if="selectedPaymentMethod === 'bnpl'" class="pt-4">
              <div class="font-medium mb-3">Installment Plan: {{ selectedPlan.months }} months</div>
              <div class="text-sm text-gray-600 space-y-2">
                <div v-for="(inst, i) in calculatedInstallments" :key="i" class="flex justify-between">
                  <span>Installment {{ i+1 }} • {{ inst.dueDate }}</span>
                  <span class="font-medium">₹{{ inst.amount }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="flex justify-between">
            <button @click="currentStep = 1" class="px-8 py-3 border rounded-xl hover:bg-gray-50">Back</button>
            <button @click="confirmTransaction" :disabled="loading" class="px-10 py-3 bg-blue-700 text-white rounded-xl hover:bg-blue-800 disabled:opacity-60">
              {{ loading ? 'Processing...' : 'Confirm & Pay' }}
            </button>
          </div>
        </div>

        <!-- STEP 3: Success -->
        <div v-if="currentStep === 3" class="text-center py-12 space-y-8">
          <div class="mx-auto w-20 h-20 bg-green-100 rounded-full flex items-center justify-center">
            <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 class="text-3xl font-bold text-gray-900">Payment Successful!</h2>
          <p class="text-gray-600 max-w-md mx-auto">
            {{ selectedPaymentMethod === 'bnpl' ? `Split into ${selectedPlan?.months} installments.` : 'Thank you for your payment.' }}
          </p>

          <div class="flex flex-col sm:flex-row gap-4 justify-center">
            <button @click="viewTransactionHistory" class="px-8 py-4 bg-white border-2 border-gray-300 rounded-2xl hover:border-blue-500">
              View Transactions
            </button>
            <button @click="goToAllInstallments" class="px-8 py-4 bg-blue-700 text-white rounded-2xl hover:bg-blue-800">
              View Installments
            </button>
            <button @click="resetForm" class="px-8 py-4 border rounded-2xl hover:bg-gray-50">
              New Payment
            </button>
          </div>
        </div>

        <!-- ===================== ALL TRANSACTIONS ===================== -->
        <div v-if="currentStep === 5" class="space-y-8">
          <h2 class="text-2xl font-semibold">All Transactions</h2>

          <div class="flex flex-wrap gap-4 items-center">
            <div class="flex gap-2">
              <button v-for="f in ['all','full','bnpl']" :key="f" @click="filterTransactions(f)" class="px-5 py-2 text-sm rounded-xl" :class="transactionFilter === f ? 'bg-blue-700 text-white' : 'bg-gray-100 hover:bg-gray-200'">
                {{ f === 'all' ? 'All' : f === 'full' ? 'Full Payment' : 'BNPL' }}
              </button>
            </div>
            <select v-model="sortOption" class="px-5 py-2.5 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500">
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

          <div v-if="loading" class="text-center py-12">Loading transactions...</div>
          <div v-else-if="!paginatedTransactions.length" class="text-center py-12 text-gray-500">No transactions found</div>
          <div v-else class="space-y-6">
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
                  <tr v-for="t in paginatedTransactions" :key="t.id" @click="selectedTransaction = t" class="hover:bg-gray-50 cursor-pointer transition-colors">
                    <td class="px-6 py-4 text-sm text-gray-700">{{ formatDate(t.transactionDate) }}</td>
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

            <!-- Transaction Detail -->
            <div v-if="selectedTransaction" class="bg-gray-50 rounded-2xl p-6 border">
              <div class="flex justify-between items-center mb-4">
                <h3 class="text-lg font-semibold">Transaction #{{ selectedTransaction.id }}</h3>
                <button @click="selectedTransaction = null" class="text-gray-500 hover:text-gray-700">× Close</button>
              </div>
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div><span class="text-gray-600">Date:</span> {{ formatDate(selectedTransaction.transactionDate) }}</div>
                <div><span class="text-gray-600">Card:</span> •••• {{ (selectedTransaction.cardNumber || 'XXXX').slice(-4) }}</div>
                <div><span class="text-gray-600">Merchant:</span> {{ selectedTransaction.merchantName || '—' }}</div>
                <div><span class="text-gray-600">Category:</span> {{ selectedTransaction.category || '—' }}</div>
                <div><span class="text-gray-600">Amount:</span> ₹{{ Number(selectedTransaction.amount).toLocaleString('en-IN') }}</div>
                <div><span class="text-gray-600">Type:</span> {{ selectedTransaction.isBNPL ? 'BNPL' : 'Paid in Full' }}</div>
                <div><span class="text-gray-600">Status:</span> {{ selectedTransaction.status || 'Completed' }}</div>
              </div>
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="flex justify-between items-center pt-4">
              <button @click="prevPage" :disabled="currentPage === 1" class="px-6 py-2 border rounded-xl disabled:opacity-40">Previous</button>
              <span class="text-sm text-gray-600">Page {{ currentPage }} of {{ totalPages }}</span>
              <button @click="nextPage" :disabled="currentPage === totalPages" class="px-6 py-2 border rounded-xl disabled:opacity-40">Next</button>
            </div>
          </div>

          <button @click="currentStep = 0" class="w-full py-4 border rounded-xl hover:bg-gray-50 mt-6">Back to New Payment</button>
        </div>

        <!-- ALL INSTALLMENTS (simplified - no search) -->
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
                <span>Total: ₹{{ group.reduce((sum, i) => sum + Number(i.amount), 0).toFixed(2) }}</span>
              </div>
              <div class="overflow-x-auto">
                <table class="min-w-full">
                  <thead class="bg-white">
                    <tr class="border-b">
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500">ID</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500">Due</th>
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

const getAuthToken = () => localStorage.getItem('authToken') || localStorage.getItem('token') || ''

const paymentSteps = ['Details', 'Options', 'Confirm', 'Complete']

const currentStep = ref(0)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const transaction = ref({
  cardNumber: '', cvv: '', expiryMonth: '', expiryYear: '', amount: '', category: '', merchantName: '', isBNPL: false
})

const categories = ref(['Electronics', 'Clothing', 'Travel', 'Home', 'Food', 'Entertainment', 'Other'])
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

  if (transactionFilter.value !== 'all') {
    const isBnpl = transactionFilter.value === 'bnpl'
    list = list.filter(t => !!t.isBNPL === isBnpl)
  }

  if (dateFilter.value.from) {
    const from = new Date(dateFilter.value.from)
    list = list.filter(t => new Date(t.transactionDate) >= from)
  }
  if (dateFilter.value.to) {
    const to = new Date(dateFilter.value.to)
    list = list.filter(t => new Date(t.transactionDate) <= to)
  }

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

const filteredInstallments = computed(() => {
  if (installmentFilter.value === 'all') return displayedInstallments.value
  return displayedInstallments.value.filter(i => {
    if (installmentFilter.value === 'paid') return i.isPaid
    if (installmentFilter.value === 'pending') return !i.isPaid && !isOverdue(i.dueDate)
    if (installmentFilter.value === 'overdue') return !i.isPaid && isOverdue(i.dueDate)
    return true
  })
})

const groupedInstallments = computed(() => {
  const map = {}
  filteredInstallments.value.forEach(i => {
    const key = i.transactionId || 'unknown'
    if (!map[key]) map[key] = []
    map[key].push(i)
  })
  return Object.values(map)
})

// ─── Methods ─────────────────────────────────────────────────────────────────
const checkEligibility = async () => {
  if (!isFormValid.value) {
    errorMessage.value = 'Please complete all required fields correctly'
    return
  }
  loading.value = true
  errorMessage.value = ''
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
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getAuthToken()}` },
      body: JSON.stringify(payload)
    })
    if (!res.ok) throw new Error('Validation failed')
    const data = await res.json()
    eligibilityResult.value = { eligible: !!data.eligible, message: data.message || '' }
    currentStep.value = 1
  } catch (err) {
    errorMessage.value = err.message || 'Could not check eligibility'
  } finally {
    loading.value = false
  }
}

const proceedToConfirmation = () => {
  if (canProceed.value) currentStep.value = 2
}

const confirmTransaction = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const token = getAuthToken()
    const payload = {
      cardNumber: transaction.value.cardNumber.replace(/\s/g, ''),
      cvv: transaction.value.cvv,
      expiryMonth: transaction.value.expiryMonth,
      expiryYear: transaction.value.expiryYear,
      amount: Number(transaction.value.amount),
      category: transaction.value.category,
      merchantName: transaction.value.merchantName,
      isBNPL: selectedPaymentMethod.value === 'bnpl'
    }
    let url = `${API_BASE_URL}/transactions`
    if (payload.isBNPL && selectedPlan.value) {
      url += `/bnpl?plan=${planMapping[selectedPlan.value.months]}`
    }
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify(payload)
    })
    if (!res.ok) throw new Error('Payment failed')
    currentStep.value = 3
    successMessage.value = 'Payment completed successfully!'
  } catch (err) {
    errorMessage.value = err.message || 'Transaction failed'
  } finally {
    loading.value = false
  }
}

const fetchTransactions = async () => {
  loading.value = true
  try {
    const res = await fetch(`${API_BASE_URL}/transactions`, {
      headers: { Authorization: `Bearer ${getAuthToken()}` }
    })
    if (!res.ok) throw new Error('Failed to load')
    const data = await res.json()
    transactions.value = Array.isArray(data) ? data : []
  } catch (err) {
    errorMessage.value = 'Could not load transactions'
  } finally {
    loading.value = false
  }
}

const viewTransactionHistory = () => {
  currentStep.value = 5
  fetchTransactions()
}

const goToAllInstallments = () => {
  currentStep.value = 6
  // fetch all installments logic here if you have endpoint for it
  // for now assuming displayedInstallments is already populated or add fetch call
}

const resetForm = () => {
  currentStep.value = 0
  transaction.value = { cardNumber: '', cvv: '', expiryMonth: '', expiryYear: '', amount: '', category: '', merchantName: '', isBNPL: false }
  selectedPaymentMethod.value = null
  selectedPlan.value = null
  errorMessage.value = ''
  successMessage.value = ''
}

const formatCardNumber = () => {
  let v = transaction.value.cardNumber.replace(/\D/g, '')
  v = v.replace(/(\d{4})/g, '$1 ').trim()
  transaction.value.cardNumber = v
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

const isOverdue = (date) => new Date(date) < new Date()

const calculateTotalAmount = (items) => items.reduce((sum, i) => sum + Number(i.amount), 0).toFixed(2)

onMounted(() => {
  // Optional: fetchTransactions() if you want to preload
})
</script>