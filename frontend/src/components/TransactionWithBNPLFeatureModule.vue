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
        <button @click="$router.push('/dashboard')" class="flex items-center gap-2 px-5 py-2.5 bg-white text-blue-700 rounded-2xl font-medium hover:bg-gray-100 transition-all active:scale-95" aria-label="Back to Dashboard">
          <span>←</span> <span>Dashboard</span>
        </button>
      </div>

      <!-- Step Indicator -->
      <div class="px-8 pt-8 pb-4">
        <div class="flex items-center justify-between" role="navigation" aria-label="BNPL Process">
          <div v-for="(stepName, index) in steps" :key="index" class="flex flex-col items-center flex-1 relative cursor-pointer" :class="{ 'opacity-40 pointer-events-none': index > currentStep }" @click="goToStep(index)">
            <div class="w-9 h-9 rounded-2xl flex items-center justify-center text-sm font-semibold transition-all" :class="[currentStep >= index ? 'bg-blue-700 text-white shadow-md' : 'bg-gray-200 text-gray-400']" :aria-current="currentStep === index ? 'step' : undefined">
              {{ index + 1 }}
            </div>
            <div class="text-xs mt-3 font-medium text-center max-w-[90px]" :class="currentStep >= index ? 'text-blue-700' : 'text-gray-400'">
              {{ stepName }}
            </div>
            <div v-if="index < steps.length - 1" class="absolute top-4 left-[50%] w-[calc(100%-2.25rem)] h-0.5 bg-gray-200 -z-10">
              <div class="h-full bg-blue-700 transition-all duration-300" :style="{ width: currentStep > index ? '100%' : '0%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="p-8">
        <!-- Messages -->
        <div v-if="errorMessage" class="mb-6 bg-red-50 border border-red-200 text-red-700 px-5 py-4 rounded-2xl text-sm flex items-start gap-3">
          <span class="text-xl">⚠️</span> <span>{{ errorMessage }}</span>
        </div>
        <div v-if="successMessage" class="mb-6 bg-green-50 border border-green-200 text-green-700 px-5 py-4 rounded-2xl text-sm flex items-start gap-3">
          <span class="text-xl">✅</span> <span>{{ successMessage }}</span>
        </div>

        <!-- STEP 0: Transaction Details -->
        <div v-if="currentStep === 0" class="space-y-8">
          <div class="flex items-center justify-between">
            <h2 class="text-2xl font-semibold text-gray-900">Transaction Details</h2>
            <span class="text-xs bg-amber-100 text-amber-700 px-3 py-1 rounded-xl">All fields mandatory</span>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="space-y-2 md:col-span-2">
              <label class="block text-sm font-medium text-gray-700">Card Number <span class="text-red-500">*</span></label>
              <input v-model="transaction.cardNumber" type="text" maxlength="19" class="w-full px-5 py-3.5 border border-gray-300 rounded-2xl focus:outline-none focus:border-blue-600 focus:ring-1 focus:ring-blue-600 text-lg" placeholder="1234 5678 9012 3456" @input="formatCardNumber" />
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">CVV <span class="text-red-500">*</span></label>
              <input v-model="transaction.cvv" type="password" maxlength="4" class="w-full px-5 py-3.5 border border-gray-300 rounded-2xl focus:outline-none focus:border-blue-600 focus:ring-1 focus:ring-blue-600" placeholder="123" />
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">Expiry Date (MM/YY) <span class="text-red-500">*</span></label>
              <div class="flex gap-3">
                <input v-model="transaction.expiryMonth" type="text" maxlength="2" class="w-1/2 px-5 py-3.5 border border-gray-300 rounded-2xl focus:outline-none focus:border-blue-600 focus:ring-1 focus:ring-blue-600 text-center" placeholder="MM" />
                <span class="self-center text-gray-400 text-xl">/</span>
                <input v-model="transaction.expiryYear" type="text" maxlength="2" class="w-1/2 px-5 py-3.5 border border-gray-300 rounded-2xl focus:outline-none focus:border-blue-600 focus:ring-1 focus:ring-blue-600 text-center" placeholder="YY" />
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">Amount (₹) <span class="text-red-500">*</span></label>
              <div class="relative">
                <span class="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400 text-xl">₹</span>
                <input v-model.number="transaction.amount" type="number" step="0.01" min="1" class="w-full pl-10 pr-5 py-3.5 border border-gray-300 rounded-2xl focus:outline-none focus:border-blue-600 focus:ring-1 focus:ring-blue-600" placeholder="5000.00" />
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">Category <span class="text-red-500">*</span></label>
              <select v-model="transaction.category" class="w-full px-5 py-3.5 border border-gray-300 rounded-2xl focus:outline-none focus:border-blue-600 focus:ring-1 focus:ring-blue-600 bg-white">
                <option value="" disabled>Select category</option>
                <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
              </select>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">Merchant <span class="text-red-500">*</span></label>
              <select v-model="transaction.merchantName" class="w-full px-5 py-3.5 border border-gray-300 rounded-2xl focus:outline-none focus:border-blue-600 focus:ring-1 focus:ring-blue-600 bg-white">
                <option value="" disabled>Select merchant</option>
                <option v-for="mer in merchants" :key="mer" :value="mer">{{ mer }}</option>
              </select>
            </div>
          </div>

          <div class="flex justify-between pt-4">
            <button @click="viewTransactionHistory" class="px-7 py-3.5 border border-gray-300 text-gray-700 rounded-2xl hover:bg-gray-50 font-medium transition-all">
              View All Transactions
            </button>
            <button @click="checkEligibility" :disabled="loading || !isFormValid" class="px-8 py-3.5 bg-blue-700 hover:bg-blue-800 disabled:bg-gray-300 text-white rounded-2xl font-semibold transition-all flex items-center gap-2">
              <span v-if="loading">Validating...</span>
              <span v-else>Continue to Payment →</span>
            </button>
          </div>
        </div>

        <!-- STEP 1: Payment Options (unchanged from your code) -->
        <div v-if="currentStep === 1" class="space-y-8">
          <h2 class="text-2xl font-semibold text-gray-900">Choose Payment Option</h2>

          <div v-if="eligibilityResult.eligible" class="space-y-6">
            <div class="bg-emerald-50 border border-emerald-200 rounded-3xl p-6 flex gap-4">
              <div class="text-emerald-600 text-3xl">✅</div>
              <div>
                <h3 class="font-semibold text-emerald-800">You are eligible for BNPL</h3>
                <p class="text-emerald-700 text-sm">Split your payment into 3, 6 or 9 easy installments.</p>
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div @click="selectPaymentMethod('full')" class="border-2 rounded-3xl p-6 cursor-pointer transition-all hover:shadow-md" :class="selectedPaymentMethod === 'full' ? 'border-blue-700 bg-blue-50' : 'border-gray-200'">
                <div class="flex items-center justify-between">
                  <div>
                    <div class="flex items-center gap-3">
                      <div class="w-6 h-6 rounded-full border-2 flex items-center justify-center" :class="selectedPaymentMethod === 'full' ? 'border-blue-700' : 'border-gray-400'">
                        <div v-if="selectedPaymentMethod === 'full'" class="w-3 h-3 bg-blue-700 rounded-full"></div>
                      </div>
                      <div class="font-semibold text-lg">Pay in Full</div>
                    </div>
                    <p class="text-gray-500 text-sm mt-1">One-time payment today</p>
                  </div>
                  <div class="text-3xl font-bold text-gray-900">₹{{ Number(transaction.amount).toLocaleString('en-IN') }}</div>
                </div>
              </div>

              <div @click="selectPaymentMethod('bnpl')" class="border-2 rounded-3xl p-6 cursor-pointer transition-all hover:shadow-md" :class="selectedPaymentMethod === 'bnpl' ? 'border-blue-700 bg-blue-50' : 'border-gray-200'">
                <div class="flex items-center justify-between">
                  <div>
                    <div class="flex items-center gap-3">
                      <div class="w-6 h-6 rounded-full border-2 flex items-center justify-center" :class="selectedPaymentMethod === 'bnpl' ? 'border-blue-700' : 'border-gray-400'">
                        <div v-if="selectedPaymentMethod === 'bnpl'" class="w-3 h-3 bg-blue-700 rounded-full"></div>
                      </div>
                      <div class="font-semibold text-lg">Buy Now Pay Later</div>
                    </div>
                    <p class="text-gray-500 text-sm mt-1">Split into installments</p>
                  </div>
                  <div class="text-blue-700 text-sm font-medium">View plans ↓</div>
                </div>
              </div>
            </div>

            <div v-if="selectedPaymentMethod === 'bnpl'" class="space-y-5">
              <h3 class="font-medium text-lg">Select Installment Plan</h3>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div v-for="plan in installmentPlans" :key="plan.months" @click="selectInstallmentPlan(plan)" class="border-2 rounded-3xl p-6 cursor-pointer transition-all hover:shadow-md" :class="selectedPlan?.months === plan.months ? 'border-blue-700 bg-blue-50' : 'border-gray-200'">
                  <div class="text-3xl font-bold">{{ plan.months }} <span class="text-base font-normal">months</span></div>
                  <div class="mt-4 text-sm text-gray-600">₹{{ (transaction.amount / plan.months).toFixed(2) }} × {{ plan.months }}</div>
                  <div v-if="plan.interestRate > 0" class="mt-3 text-xs text-amber-600">{{ plan.interestRate }}% interest</div>
                  <div v-else class="mt-3 text-xs text-emerald-600 font-medium">Zero interest</div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="bg-amber-50 border border-amber-200 rounded-3xl p-8 text-center">
            <div class="text-amber-500 text-4xl mb-3">⚠️</div>
            <h3 class="font-semibold text-amber-800">Not eligible for installments right now</h3>
            <p class="text-amber-700 mt-2">{{ eligibilityResult.message || 'Try full payment or check eligibility later.' }}</p>
          </div>

          <div class="flex justify-between pt-6">
            <button @click="currentStep = 0" class="px-7 py-3.5 border border-gray-300 text-gray-700 rounded-2xl hover:bg-gray-50 font-medium">← Back</button>
            <button @click="proceedToConfirmation" :disabled="!canProceed || loading" class="px-10 py-3.5 bg-blue-700 hover:bg-blue-800 disabled:bg-gray-300 text-white rounded-2xl font-semibold transition-all">
              <span v-if="loading">Processing...</span>
              <span v-else>Continue to Confirm</span>
            </button>
          </div>
        </div>

        <!-- STEP 2: Confirmation (unchanged) -->
        <div v-if="currentStep === 2" class="space-y-8">
          <h2 class="text-2xl font-semibold text-gray-900">Confirm Transaction</h2>
          <div class="bg-gray-50 rounded-3xl p-8 space-y-6">
            <div class="flex justify-between items-center border-b pb-6">
              <span class="text-gray-600">Card ending in</span>
              <span class="font-mono font-semibold">•••• {{ transaction.cardNumber.slice(-4) || 'XXXX' }}</span>
            </div>
            <div class="flex justify-between items-center border-b pb-6">
              <span class="text-gray-600">Amount</span>
              <span class="font-semibold text-xl">₹{{ Number(transaction.amount).toLocaleString('en-IN') }}</span>
            </div>
            <div class="flex justify-between items-center border-b pb-6">
              <span class="text-gray-600">Merchant</span>
              <span class="font-medium">{{ transaction.merchantName }}</span>
            </div>
            <div class="flex justify-between items-center border-b pb-6">
              <span class="text-gray-600">Category</span>
              <span class="font-medium">{{ transaction.category }}</span>
            </div>

            <div v-if="selectedPaymentMethod === 'bnpl' && selectedPlan" class="pt-4 border-t">
              <div class="flex justify-between mb-4">
                <span class="font-medium">Installment Plan</span>
                <span class="font-medium">{{ selectedPlan.months }} months @ ₹{{ (transaction.amount / selectedPlan.months).toFixed(2) }}</span>
              </div>
              <div class="text-xs text-gray-500 bg-white p-5 rounded-2xl space-y-3">
                <div v-for="(inst, i) in calculatedInstallments" :key="i" class="flex justify-between">
                  <span>Installment {{ i + 1 }} • {{ inst.dueDate }}</span>
                  <span class="font-medium">₹{{ inst.amount }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="flex justify-between">
            <button @click="currentStep = 1" class="px-7 py-3.5 border border-gray-300 text-gray-700 rounded-2xl hover:bg-gray-50 font-medium">← Back</button>
            <button @click="confirmTransaction" :disabled="loading" class="px-10 py-3.5 bg-blue-700 hover:bg-blue-800 text-white rounded-2xl font-semibold transition-all">
              <span v-if="loading">Confirming...</span>
              <span v-else>Confirm & Pay</span>
            </button>
          </div>
        </div>

        <!-- STEP 3: Success -->
        <div v-if="currentStep === 3" class="py-12 text-center space-y-8">
          <div class="mx-auto w-24 h-24 bg-emerald-100 rounded-full flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-14 h-14 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <div>
            <h2 class="text-3xl font-bold text-gray-900">Transaction Completed!</h2>
            <p class="text-gray-600 mt-3 max-w-md mx-auto">
              Your payment has been successfully processed.
              {{ selectedPaymentMethod === 'bnpl' ? `It has been split into ${selectedPlan?.months} installments.` : 'Thank you for your payment.' }}
            </p>
          </div>
          <div class="flex flex-col md:flex-row gap-4 justify-center pt-6">
            <button @click="viewTransactionHistory" class="flex-1 max-w-xs mx-auto px-8 py-4 bg-white border-2 border-gray-300 hover:border-gray-400 rounded-3xl font-medium flex items-center justify-center gap-3 transition-all">
              <span class="text-xl">📋</span> <span>View All Transactions</span>
            </button>
            <button v-if="selectedPaymentMethod === 'bnpl'" @click="goToInstallments" class="flex-1 max-w-xs mx-auto px-8 py-4 bg-blue-700 hover:bg-blue-800 text-white rounded-3xl font-medium flex items-center justify-center gap-3 transition-all">
              <span class="text-xl">📆</span> <span>Manage Installments</span>
            </button>
          </div>
          <button @click="resetForm" class="text-blue-700 underline hover:text-blue-800 text-sm font-medium mt-8">
            Make Another Payment
          </button>
        </div>

        <!-- STEP 4: My Installments -->
        <div v-if="currentStep === 4" class="space-y-8">
          <div class="flex items-center justify-between">
            <h2 class="text-2xl font-semibold">My Installments</h2>
            <div class="flex gap-2">
              <button v-for="f in ['all','pending','paid','overdue']" :key="f" @click="filterInstallments(f)" class="px-5 py-2 text-sm font-medium rounded-2xl transition-all" :class="installmentFilter === f ? 'bg-blue-700 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'">
                {{ f.charAt(0).toUpperCase() + f.slice(1) }}
              </button>
            </div>
          </div>

          <div class="flex gap-3 items-end">
            <div class="flex-1">
              <label class="text-sm font-medium text-gray-600 block mb-1">Transaction ID</label>
              <div class="flex">
                <input v-model="searchTransactionId" type="text" class="flex-1 px-5 py-3.5 border border-gray-300 rounded-l-3xl focus:outline-none focus:border-blue-600" placeholder="Enter Transaction ID" />
                <button @click="fetchInstallments" :disabled="loading || !searchTransactionId" class="bg-blue-700 text-white px-8 rounded-r-3xl font-medium">Search</button>
              </div>
            </div>
            <button @click="viewTransactionHistory" class="px-6 py-3.5 border border-gray-300 rounded-3xl text-gray-700 hover:bg-gray-50">All Transactions</button>
          </div>

          <div v-if="Object.keys(groupedDisplayedInstallments).length === 0" class="text-center py-16 bg-gray-50 rounded-3xl">
            <p class="text-gray-400">No installments found. Enter a valid Transaction ID above.</p>
          </div>

          <div v-else class="space-y-10">
            <div v-for="(group, transId) in groupedDisplayedInstallments" :key="transId" class="border border-gray-200 rounded-3xl overflow-hidden">
              <div class="bg-gray-50 px-8 py-5 font-medium flex justify-between">
                <span>Transaction #{{ transId }}</span>
                <span class="text-emerald-600">Total: ₹{{ calculateTotalAmount(group) }}</span>
              </div>
              <div class="overflow-x-auto">
                <table class="min-w-full">
                  <thead class="bg-white">
                    <tr class="border-b">
                      <th class="px-8 py-4 text-left text-xs font-medium text-gray-500">INST. ID</th>
                      <th class="px-8 py-4 text-left text-xs font-medium text-gray-500">NO.</th>
                      <th class="px-8 py-4 text-left text-xs font-medium text-gray-500">AMOUNT</th>
                      <th class="px-8 py-4 text-left text-xs font-medium text-gray-500">DUE DATE</th>
                      <th class="px-8 py-4 text-left text-xs font-medium text-gray-500">STATUS</th>
                      <th class="px-8 py-4 text-left text-xs font-medium text-gray-500">LATE FEE</th>
                      <th class="px-8 py-4 text-right text-xs font-medium text-gray-500">ACTION</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y">
                    <tr v-for="inst in group" :key="inst.id" class="hover:bg-gray-50">
                      <td class="px-8 py-5 font-mono text-sm">{{ inst.id }}</td>
                      <td class="px-8 py-5 text-sm text-gray-500">{{ inst.installmentNumber }}</td>
                      <td class="px-8 py-5 font-medium">₹{{ inst.amount.toFixed(2) }}</td>
                      <td class="px-8 py-5 text-sm text-gray-500">{{ formatDate(inst.dueDate) }}</td>
                      <td class="px-8 py-5">
                        <span class="inline-flex px-4 py-1 text-xs font-medium rounded-3xl" :class="inst.isPaid ? 'bg-emerald-100 text-emerald-700' : isOverdue(inst.dueDate) ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700'">
                          {{ inst.isPaid ? 'Paid' : isOverdue(inst.dueDate) ? 'Overdue' : 'Pending' }}
                        </span>
                      </td>
                      <td class="px-8 py-5 text-sm text-gray-500">₹{{ inst.lateFee.toFixed(2) }}</td>
                      <td class="px-8 py-5 text-right">
                        <div v-if="confirmingInstallment?.id === inst.id" class="flex items-center gap-3 justify-end text-sm">
                          <span class="text-gray-700">Confirm ₹{{ Number(inst.amount).toLocaleString('en-IN') }}?</span>
                          <button @click="payInstallment(inst)" class="px-5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl">Yes, Pay</button>
                          <button @click="confirmingInstallment = null" class="px-4 py-1.5 bg-gray-200 hover:bg-gray-300 rounded-xl">Cancel</button>
                        </div>
                        <button v-else-if="!inst.isPaid" @click="payInstallment(inst)" class="px-6 py-2 bg-blue-700 text-white text-sm rounded-2xl hover:bg-blue-800 disabled:opacity-50" :disabled="loading">
                          Pay Now
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <button @click="resetForm" class="w-full py-4 border border-gray-300 rounded-3xl text-gray-700 font-medium">New Payment</button>
        </div>

        <!-- STEP 5: All Transactions – Updated -->
        <div v-if="currentStep === 5" class="space-y-8">
          <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <h2 class="text-2xl font-semibold">All Transactions</h2>
            <div class="flex flex-wrap items-center gap-4">
              <div class="flex gap-2">
                <button v-for="f in ['all','full','bnpl']" :key="f" @click="filterTransactions(f)" class="px-5 py-2 text-sm font-medium rounded-2xl transition-all" :class="transactionFilter === f ? 'bg-blue-700 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'">
                  {{ f === 'full' ? 'Paid in Full' : f === 'bnpl' ? 'BNPL' : 'All' }}
                </button>
              </div>

              <select v-model="sortOption" class="px-5 py-2.5 border border-gray-300 rounded-2xl focus:outline-none focus:border-blue-600 text-sm min-w-[180px]">
                <option value="newest">Newest first</option>
                <option value="oldest">Oldest first</option>
                <option value="amount-high">Amount high to low</option>
                <option value="amount-low">Amount low to high</option>
                <option value="merchant">Merchant A-Z</option>
              </select>
            </div>
          </div>

          <!-- Date Range -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium mb-2">From Date</label>
              <input v-model="dateFilter.from" type="date" class="w-full px-5 py-3.5 border border-gray-300 rounded-3xl focus:outline-none focus:border-blue-600" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">To Date</label>
              <div class="flex gap-3 items-center">
                <input v-model="dateFilter.to" type="date" class="flex-1 px-5 py-3.5 border border-gray-300 rounded-3xl focus:outline-none focus:border-blue-600" />
                <button @click="clearDateFilter" class="text-gray-500 hover:text-gray-700 text-sm">Clear</button>
              </div>
            </div>
          </div>

          <!-- Loading / Empty / Table -->
          <div v-if="loading" class="text-center py-12 text-gray-500">Loading your transactions...</div>

          <div v-else-if="paginatedTransactions.length === 0" class="text-center py-16 bg-gray-50 rounded-3xl text-gray-400">
            No transactions found
          </div>

          <div v-else class="overflow-x-auto rounded-3xl border border-gray-100">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-8 py-5 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                  <th class="px-8 py-5 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                  <th class="px-8 py-5 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Card</th>
                  <th class="px-8 py-5 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Merchant</th>
                  <th class="px-8 py-5 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                  <th class="px-8 py-5 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Method</th>
                  <th class="px-8 py-5 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                  <th class="px-8 py-5 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="t in paginatedTransactions" :key="t.id" class="hover:bg-gray-50 transition-colors">
                  <td class="px-8 py-6 whitespace-nowrap font-mono text-sm text-gray-900">#{{ t.id }}</td>
                  <td class="px-8 py-6 whitespace-nowrap text-sm text-gray-600">{{ formatDate(t.transactionDate) }}</td>
                  <td class="px-8 py-6 whitespace-nowrap font-mono text-sm text-gray-600">•••• {{ (t.cardNumber || 'XXXX').slice(-4) }}</td>
                  <td class="px-8 py-6 whitespace-nowrap text-sm text-gray-900">{{ t.merchantName || '—' }}</td>
                  <td class="px-8 py-6 whitespace-nowrap text-sm font-semibold text-gray-900">₹{{ Number(t.amount).toLocaleString('en-IN') }}</td>
                  <td class="px-8 py-6 whitespace-nowrap">
                    <span class="inline-flex px-3 py-1 text-xs font-medium rounded-full" :class="t.isBNPL ? 'bg-blue-100 text-blue-800' : 'bg-emerald-100 text-emerald-800'">
                      {{ t.isBNPL ? 'BNPL' : 'Full Payment' }}
                    </span>
                  </td>
                  <td class="px-8 py-6 whitespace-nowrap">
                    <span class="inline-flex px-3 py-1 text-xs font-medium rounded-full" :class="getStatusClass(t.status)">
                      {{ t.status || 'Completed' }}
                    </span>
                  </td>
                  <td class="px-8 py-6 whitespace-nowrap text-right text-sm font-medium">
                    <button v-if="t.isBNPL" @click="viewTransactionInstallments(t.id)" class="text-blue-700 hover:text-blue-900 font-medium">
                      View Installments →
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="flex items-center justify-between border-t border-gray-200 pt-4">
            <button @click="prevPage" :disabled="currentPage === 1" class="px-6 py-2 border border-gray-300 rounded-xl text-sm font-medium disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50">
              Previous
            </button>

            <div class="flex gap-2">
              <button v-for="p in paginationRange" :key="p" @click="goToPage(p)" class="w-10 h-10 flex items-center justify-center rounded-xl text-sm font-medium transition-colors" :class="currentPage === p ? 'bg-blue-700 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'">
                {{ p }}
              </button>
            </div>

            <button @click="nextPage" :disabled="currentPage === totalPages" class="px-6 py-2 border border-gray-300 rounded-xl text-sm font-medium disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50">
              Next
            </button>
          </div>

          <button @click="resetForm" class="mt-10 w-full py-4 text-blue-700 hover:text-blue-800 underline text-sm font-medium">
            Make Another Payment
          </button>
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

const steps = ['Transaction Details', 'Payment Options', 'Confirmation', 'Complete', 'My Installments', 'All Transactions']

const currentStep = ref(0)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const transaction = ref({
  cardNumber: '',
  cvv: '',
  expiryMonth: '',
  expiryYear: '',
  amount: '',
  category: '',
  merchantName: '',
  isBNPL: false
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

const displayedInstallments = ref([])
const searchTransactionId = ref('')
const installmentFilter = ref('all')

const transactions = ref([])
const transactionFilter = ref('all')
const dateFilter = ref({ from: '', to: '' })
const sortOption = ref('newest') // default sorting

const currentPage = ref(1)
const pageSize = 10

const confirmingInstallment = ref(null)

// ─── Computed ────────────────────────────────────────────────────────────────
const filteredTransactions = computed(() => {
  let list = [...transactions.value]

  if (transactionFilter.value !== 'all') {
    list = list.filter(t => (t.isBNPL ? 'bnpl' : 'full') === transactionFilter.value)
  }

  if (dateFilter.value.from) {
    const from = new Date(dateFilter.value.from)
    list = list.filter(t => new Date(t.transactionDate) >= from)
  }
  if (dateFilter.value.to) {
    const to = new Date(dateFilter.value.to)
    list = list.filter(t => new Date(t.transactionDate) <= to)
  }

  // Sorting
  const sorted = [...list]
  if (sortOption.value === 'newest') {
    sorted.sort((a, b) => new Date(b.transactionDate) - new Date(a.transactionDate))
  } else if (sortOption.value === 'oldest') {
    sorted.sort((a, b) => new Date(a.transactionDate) - new Date(b.transactionDate))
  } else if (sortOption.value === 'amount-high') {
    sorted.sort((a, b) => Number(b.amount) - Number(a.amount))
  } else if (sortOption.value === 'amount-low') {
    sorted.sort((a, b) => Number(a.amount) - Number(b.amount))
  } else if (sortOption.value === 'merchant') {
    sorted.sort((a, b) => (a.merchantName || '').localeCompare(b.merchantName || ''))
  }

  return sorted
})

const paginatedTransactions = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredTransactions.value.slice(start, start + pageSize)
})

const totalPages = computed(() => Math.ceil(filteredTransactions.value.length / pageSize))

const paginationRange = computed(() => {
  const max = 5
  let start = Math.max(1, currentPage.value - Math.floor(max / 2))
  let end = Math.min(totalPages.value, start + max - 1)
  if (end - start + 1 < max) start = Math.max(1, end - max + 1)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

const canProceed = computed(() => selectedPaymentMethod.value === 'full' || (selectedPaymentMethod.value === 'bnpl' && selectedPlan.value))

const calculatedInstallments = computed(() => {
  if (!selectedPlan.value) return []
  const amt = parseFloat(transaction.value.amount || 0)
  const months = selectedPlan.value.months
  const perMonth = (amt / months).toFixed(2)
  const today = new Date()
  const arr = []
  for (let i = 0; i < months; i++) {
    const due = new Date(today)
    due.setMonth(today.getMonth() + i + 1)
    arr.push({
      amount: perMonth,
      dueDate: due.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
    })
  }
  return arr
})

const isFormValid = computed(() => {
  const cleanCard = (transaction.value.cardNumber || '').replace(/\s/g, '')
  return (
    cleanCard.length === 16 &&
    /^\d+$/.test(cleanCard) &&
    (transaction.value.cvv || '').length >= 3 &&
    /^\d+$/.test(transaction.value.cvv || '') &&
    /^(0[1-9]|1[0-2])$/.test(transaction.value.expiryMonth || '') &&
    /^\d{2}$/.test(transaction.value.expiryYear || '') &&
    Number(transaction.value.amount) > 0 &&
    transaction.value.category &&
    transaction.value.merchantName
  )
})

const filteredDisplayedInstallments = computed(() => {
  return displayedInstallments.value.filter(inst => {
    if (installmentFilter.value === 'all') return true
    const status = inst.isPaid ? 'paid' : isOverdue(inst.dueDate) ? 'overdue' : 'pending'
    return status === installmentFilter.value
  })
})

const groupedDisplayedInstallments = computed(() => {
  const groups = {}
  filteredDisplayedInstallments.value.forEach(i => {
    const tid = i.transactionId || searchTransactionId.value || 'Unknown'
    if (!groups[tid]) groups[tid] = []
    groups[tid].push(i)
  })
  return groups
})

// ─── API Calls ───────────────────────────────────────────────────────────────
const fetchTransactions = async () => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const token = getAuthToken()
    const res = await fetch(`${API_BASE_URL}/transactions`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    if (!res.ok) {
      if (res.status === 401) {
        localStorage.clear()
        router.push('/login')
        return
      }
      throw new Error('Failed to load transactions')
    }
    const data = await res.json()
    transactions.value = Array.isArray(data) ? data : (data.transactions || [])
    currentPage.value = 1
  } catch (err) {
    errorMessage.value = err.message || 'Could not load transactions'
  } finally {
    loading.value = false
  }
}

const fetchInstallments = async () => {
  if (!searchTransactionId.value?.trim()) {
    errorMessage.value = 'Please enter a Transaction ID'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const token = getAuthToken()
    const res = await fetch(`${API_BASE_URL}/bnpl/installments/transaction/${searchTransactionId.value.trim()}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (!res.ok) throw new Error('Failed to load installments')
    const data = await res.json()
    displayedInstallments.value = Array.isArray(data) ? data : (data.installments || [])
  } catch (err) {
    errorMessage.value = err.message || 'Could not load installments'
  } finally {
    loading.value = false
  }
}

const payInstallment = async (inst) => {
  if (confirmingInstallment.value?.id === inst.id) {
    loading.value = true
    errorMessage.value = ''
    successMessage.value = ''
    try {
      const token = getAuthToken()
      const res = await fetch(`${API_BASE_URL}/bnpl/installments/${inst.id}/pay?amount=${inst.amount}`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` }
      })
      if (!res.ok) throw new Error('Payment failed')
      successMessage.value = `Installment paid successfully! Amount: ₹${Number(inst.amount).toLocaleString('en-IN')}`
      confirmingInstallment.value = null
      await fetchInstallments()
    } catch (err) {
      errorMessage.value = err.message || 'Installment payment failed'
    } finally {
      loading.value = false
    }
  } else {
    confirmingInstallment.value = inst
    successMessage.value = ''
    errorMessage.value = ''
  }
}

// ─── Navigation & Helpers ───────────────────────────────────────────────────
const viewTransactionHistory = () => {
  currentStep.value = 5
  fetchTransactions()
}

const goToInstallments = () => {
  const lastId = localStorage.getItem('lastBnplTransactionId')
  if (lastId) searchTransactionId.value = lastId
  currentStep.value = 4
  if (searchTransactionId.value) fetchInstallments()
}

const viewTransactionInstallments = (id) => {
  searchTransactionId.value = String(id)
  currentStep.value = 4
  fetchInstallments()
}

const goToStep = (index) => {
  if (index <= currentStep.value) currentStep.value = index
}

const formatCardNumber = () => {
  let val = transaction.value.cardNumber.replace(/\s/g, '')
  val = val.replace(/(\d{4})/g, '$1 ').trim()
  transaction.value.cardNumber = val
}

const proceedToConfirmation = () => {
  if (canProceed.value) currentStep.value = 2
}

const selectPaymentMethod = (method) => {
  selectedPaymentMethod.value = method
  transaction.value.isBNPL = method === 'bnpl'
  if (method === 'full') selectedPlan.value = null
}

const selectInstallmentPlan = (plan) => selectedPlan.value = plan

const filterInstallments = (f) => installmentFilter.value = f
const filterTransactions = (f) => {
  transactionFilter.value = f
  currentPage.value = 1
}

const clearDateFilter = () => {
  dateFilter.value = { from: '', to: '' }
  currentPage.value = 1
}

const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }
const goToPage = (p) => currentPage.value = p

const calculateTotalAmount = (arr) => arr.reduce((sum, i) => sum + Number(i.amount || 0), 0).toFixed(2)
const isOverdue = (d) => new Date(d) < new Date()
const formatDate = (d) => new Date(d).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })

const getStatusClass = (s) => {
  if (s === 'Completed') return 'bg-emerald-100 text-emerald-800'
  if (s === 'Pending') return 'bg-amber-100 text-amber-800'
  return 'bg-gray-100 text-gray-600'
}

const resetForm = () => {
  currentStep.value = 0
  transaction.value = { cardNumber: '', cvv: '', expiryMonth: '', expiryYear: '', amount: '', category: '', merchantName: '', isBNPL: false }
  eligibilityResult.value = { eligible: false, message: '' }
  selectedPaymentMethod.value = null
  selectedPlan.value = null
  displayedInstallments.value = []
  searchTransactionId.value = ''
  transactions.value = []
  errorMessage.value = ''
  successMessage.value = ''
  confirmingInstallment.value = null
  sortOption.value = 'newest'
}

onMounted(() => {
  // Optional: you can call fetchTransactions() here if you want to pre-load
})
</script>

<style>
/* Optional: small visual improvements */
table {
  border-collapse: separate;
  border-spacing: 0;
}
th, td {
  vertical-align: middle;
}
</style>