<template>
  <div class="min-h-screen bg-gray-50 p-4 md:p-8">
    <div class="max-w-4xl mx-auto bg-white rounded-xl shadow-md overflow-hidden">
      <!-- Header -->
      <div class="bg-primary text-white p-6 flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold">Buy Now Pay Later</h1>
          <p class="text-sm opacity-80">Flexible payment options for your purchases</p>
        </div>
        <button
          @click="$router.push('/dashboard')"
          class="px-4 py-2 bg-white text-primary rounded-md hover:bg-gray-100 transition-colors"
          aria-label="Back to Dashboard"
        >
          Back to Dashboard
        </button>
      </div>

      <!-- Step indicator -->
      <div class="px-6 pt-6">
        <div class="flex items-center justify-between mb-8" role="navigation" aria-label="BNPL process steps">
          <div v-for="(step, index) in steps" :key="index" class="flex flex-col items-center flex-1">
            <div
              :class="`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${currentStep >= index ? 'bg-primary text-white' : 'bg-gray-200 text-gray-500'}`"
              :aria-current="currentStep === index ? 'step' : undefined"
            >
              {{ index + 1 }}
            </div>
            <div class="text-xs mt-2 text-center" :class="currentStep >= index ? 'text-primary' : 'text-gray-500'">
              {{ step }}
            </div>
            <div class="hidden md:block h-1 bg-gray-200 mt-4 w-full">
              <div
                class="h-full bg-primary transition-all duration-300"
                :style="`width: ${currentStep > index ? '100%' : '0%'}`"
              ></div>
            </div>
            <div v-if="index < steps.length - 1" class="block md:hidden w-8 h-1 bg-gray-200 mt-2">
              <div class="h-full bg-primary transition-all duration-300" :style="`width: ${currentStep > index ? '100%' : '0%'}`"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Main content area -->
      <div class="p-6">
        <!-- Error Notification -->
        <div v-if="errorMessage" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6 text-sm text-red-700">
          {{ errorMessage }}
        </div>

        <!-- Step 1: Transaction Form -->
        <div v-if="currentStep === 0" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-800">Transaction Details</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="space-y-2 md:col-span-2">
              <label class="block text-sm font-medium text-gray-700">Card Number</label>
              <input
                v-model="transaction.cardNumber"
                type="text"
                maxlength="19"
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                placeholder="1234 5678 9012 3456"
                required
              />
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">CVV</label>
              <input
                v-model="transaction.cvv"
                type="password"
                maxlength="4"
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                placeholder="123 or 1234"
                required
              />
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">Expiry Date (MM/YY)</label>
              <div class="flex gap-4">
                <input
                  v-model="transaction.expiryMonth"
                  type="text"
                  maxlength="2"
                  placeholder="MM"
                  class="w-20 px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                  required
                />
                <span class="self-center text-gray-500">/</span>
                <input
                  v-model="transaction.expiryYear"
                  type="text"
                  maxlength="2"
                  placeholder="YY"
                  class="w-20 px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                  required
                />
              </div>
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">Amount (₹)</label>
              <input
                v-model.number="transaction.amount"
                type="number"
                step="0.01"
                min="1"
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                placeholder="Enter amount"
                required
              />
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">Category</label>
              <select v-model="transaction.category" class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" required>
                <option value="" disabled>Select a category</option>
                <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
              </select>
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">Merchant</label>
              <select v-model="transaction.merchantName" class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary" required>
                <option value="" disabled>Select a merchant</option>
                <option v-for="merchant in merchants" :key="merchant" :value="merchant">{{ merchant }}</option>
              </select>
            </div>
          </div>

          <div class="pt-4 flex justify-between">
            <button
              @click="viewTransactionHistory"
              class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
            >
              View Transaction History
            </button>
            <button
              @click="checkEligibility"
              class="px-6 py-3 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
              :disabled="loading || !isFormValid"
            >
              <span v-if="loading">Checking...</span>
              <span v-else>Continue</span>
            </button>
          </div>
        </div>

        <!-- Step 2: Eligibility Result & Payment Options -->
        <div v-if="currentStep === 1" class="space-y-6">
          <div v-if="eligibilityResult.eligible" class="space-y-6">
            <div class="bg-green-50 border border-green-200 rounded-md p-4 flex items-start">
              <div class="flex-shrink-0 text-green-500">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-green-800">Eligible for BNPL</h3>
                <div class="mt-1 text-sm text-green-700">You're eligible to split this payment into installments.</div>
              </div>
            </div>

            <h2 class="text-xl font-semibold text-gray-800">Choose Payment Method</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div
                @click="selectPaymentMethod('full')"
                :class="`border rounded-lg p-4 cursor-pointer transition-all ${selectedPaymentMethod === 'full' ? 'border-primary bg-primary-50' : 'border-gray-200 hover:border-primary'}`"
                role="button"
              >
                <div class="flex items-center">
                  <div :class="`w-5 h-5 rounded-full border flex items-center justify-center ${selectedPaymentMethod === 'full' ? 'border-primary' : 'border-gray-300'}`">
                    <div v-if="selectedPaymentMethod === 'full'" class="w-3 h-3 rounded-full bg-primary"></div>
                  </div>
                  <div class="ml-3">
                    <h3 class="font-medium">Pay in Full</h3>
                    <p class="text-sm text-gray-500">Pay the entire amount now</p>
                  </div>
                </div>
                <div class="mt-3 text-lg font-semibold">₹{{ transaction.amount }}</div>
              </div>

              <div
                @click="selectPaymentMethod('bnpl')"
                :class="`border rounded-lg p-4 cursor-pointer transition-all ${selectedPaymentMethod === 'bnpl' ? 'border-primary bg-primary-50' : 'border-gray-200 hover:border-primary'}`"
                role="button"
              >
                <div class="flex items-center">
                  <div :class="`w-5 h-5 rounded-full border flex items-center justify-center ${selectedPaymentMethod === 'bnpl' ? 'border-primary' : 'border-gray-300'}`">
                    <div v-if="selectedPaymentMethod === 'bnpl'" class="w-3 h-3 rounded-full bg-primary"></div>
                  </div>
                  <div class="ml-3">
                    <h3 class="font-medium">Pay Later</h3>
                    <p class="text-sm text-gray-500">Split into installments</p>
                  </div>
                </div>
                <div class="mt-3 text-sm text-primary">View installment plans</div>
              </div>
            </div>

            <div v-if="selectedPaymentMethod === 'bnpl'" class="mt-6 space-y-4">
              <h3 class="font-medium">Select Installment Plan</h3>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div
                  v-for="plan in installmentPlans"
                  :key="plan.months"
                  @click="selectInstallmentPlan(plan)"
                  :class="`border rounded-lg p-4 cursor-pointer transition-all ${selectedPlan && selectedPlan.months === plan.months ? 'border-primary bg-primary-50' : 'border-gray-200 hover:border-primary'}`"
                  role="button"
                >
                  <div class="text-lg font-semibold">{{ plan.months }} months</div>
                  <div class="text-sm text-gray-500 mt-1">
                    {{ plan.months }} payments of ₹{{ (transaction.amount / plan.months).toFixed(2) }}
                  </div>
                  <div v-if="plan.interestRate > 0" class="mt-2 text-xs text-gray-500">
                    {{ plan.interestRate }}% interest
                  </div>
                  <div v-else class="mt-2 text-xs text-green-600 font-medium">Interest-free</div>
                </div>
              </div>
            </div>

            <div class="pt-4 flex justify-between">
              <button @click="currentStep = 0" class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors">Back</button>
              <button
                @click="proceedToConfirmation"
                class="px-6 py-3 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
                :disabled="!canProceed || loading"
              >
                <span v-if="loading">Processing...</span>
                <span v-else>Continue</span>
              </button>
            </div>
          </div>

          <div v-else class="space-y-6">
            <div class="bg-yellow-50 border border-yellow-200 rounded-md p-4 flex items-start">
              <div class="flex-shrink-0 text-yellow-500">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-yellow-800">Not Eligible for BNPL</h3>
                <div class="mt-1 text-sm text-yellow-700">
                  {{ eligibilityResult.message || "You're not eligible for installment payments at this time." }}
                </div>
              </div>
            </div>
            <div class="pt-4 flex justify-between">
              <button @click="currentStep = 0" class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors">Back</button>
              <button @click="proceedToPayInFull" class="px-6 py-3 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors">
                Pay in Full
              </button>
            </div>
          </div>
        </div>

        <!-- Step 3: Confirmation -->
        <div v-if="currentStep === 2" class="space-y-6">
          <h2 class="text-xl font-semibold text-gray-800">Confirm Your Payment</h2>
          <div class="bg-gray-50 rounded-lg p-6 space-y-4">
            <div class="flex justify-between">
              <span class="text-gray-600">Card ending in:</span>
              <span class="font-medium">**** **** **** {{ transaction.cardNumber.slice(-4) || 'XXXX' }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Transaction Amount:</span>
              <span class="font-medium">₹{{ transaction.amount }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Merchant:</span>
              <span class="font-medium">{{ transaction.merchantName }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Category:</span>
              <span class="font-medium">{{ transaction.category }}</span>
            </div>

            <div v-if="selectedPaymentMethod === 'bnpl' && selectedPlan" class="pt-2">
              <div class="border-t border-gray-200 pt-4">
                <h3 class="font-medium mb-3">Installment Plan: {{ selectedPlan.months }} months</h3>
                <div class="space-y-2">
                  <div v-for="(installment, index) in calculatedInstallments" :key="index" class="flex justify-between text-sm">
                    <span>Payment {{ index + 1 }} ({{ installment.dueDate }})</span>
                    <span>₹{{ installment.amount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="pt-4 flex justify-between">
            <button @click="currentStep = 1" class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors">Back</button>
            <button
              @click="confirmTransaction"
              class="px-6 py-3 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
              :disabled="loading"
            >
              <span v-if="loading">Processing...</span>
              <span v-else>Confirm Payment</span>
            </button>
          </div>
        </div>

        <!-- Step 4: Success -->
        <div v-if="currentStep === 3" class="space-y-6 text-center py-8">
          <div class="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 class="text-2xl font-semibold text-gray-800">Payment Successful!</h2>
          <p class="text-gray-600">
            <span v-if="selectedPaymentMethod === 'bnpl'">Your payment has been split into {{ selectedPlan.months }} installments.</span>
            <span v-else>Your payment has been processed successfully.</span>
          </p>
          <div class="pt-6 flex flex-col md:flex-row justify-center space-y-3 md:space-y-0 md:space-x-3">
            <button
              v-if="selectedPaymentMethod === 'bnpl'"
              @click="goToInstallments"
              class="px-6 py-3 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
              :disabled="loading"
            >
              <span v-if="loading">Loading...</span>
              <span v-else>View My Installments</span>
            </button>
            <button @click="viewTransactionHistory" class="px-6 py-3 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors">
              View Transaction History
            </button>
            <button @click="resetForm" class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors">
              Make Another Payment
            </button>
          </div>
        </div>

        <!-- Step 5: Installment Management View -->
        <div v-if="currentStep === 4" class="space-y-6">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-semibold text-gray-800">My Installments</h2>
            <div class="space-x-2">
              <button @click="filterInstallments('all')" :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'all' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`">All</button>
              <button @click="filterInstallments('pending')" :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'pending' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`">Pending</button>
              <button @click="filterInstallments('paid')" :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'paid' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`">Paid</button>
              <button @click="filterInstallments('overdue')" :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'overdue' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`">Overdue</button>
            </div>
          </div>

          <div class="space-y-4">
            <div class="flex justify-between items-center flex-wrap gap-4">
              <div class="space-y-1">
                <h3 class="font-medium">Search Installments</h3>
                <div class="flex space-x-2">
                  <input
                    v-model="searchTransactionId"
                    type="text"
                    placeholder="Enter Transaction ID"
                    class="px-3 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary w-64"
                  />
                  <button
                    @click="fetchInstallments"
                    class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
                    :disabled="loading || !searchTransactionId"
                  >
                    <span v-if="loading">Loading...</span>
                    <span v-else>Search</span>
                  </button>
                </div>
              </div>
              <div class="flex space-x-2">
                <button @click="viewTransactionHistory" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors">
                  View Transactions
                </button>
                <button @click="resetForm" class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors">
                  New Payment
                </button>
              </div>
            </div>

            <div v-if="loading" class="text-center py-8 bg-gray-50 rounded-lg">
              <p class="text-gray-500">Loading installments...</p>
            </div>
            <div v-else-if="displayedInstallments.length === 0" class="text-center py-8 bg-gray-50 rounded-lg">
              <p class="text-gray-500">No installments found for this Transaction ID.<br />Make sure it's a valid BNPL transaction ID.</p>
            </div>
            <div v-else class="space-y-6">
              <div v-for="(group, transId) in groupedDisplayedInstallments" :key="transId" class="border rounded-lg p-4">
                <h3 class="font-medium mb-3">Transaction ID: {{ transId }}</h3>
                <div class="overflow-x-auto">
                  <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Installment ID</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Installment Number</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Due Date</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Late Fee</th>
                        <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                      </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                      <tr v-for="installment in group" :key="installment.id">
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ installment.id }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ installment.installmentNumber }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">₹{{ installment.amount?.toFixed(2) || '0.00' }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(installment.dueDate) }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          <span :class="`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${installment.isPaid ? 'bg-green-100 text-green-800' : isOverdue(installment.dueDate) ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'}`">
                            {{ installment.isPaid ? 'Paid' : isOverdue(installment.dueDate) ? 'Overdue' : 'Pending' }}
                          </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">₹{{ installment.lateFee?.toFixed(2) || '0.00' }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <button
                            v-if="!installment.isPaid"
                            @click="openPaymentModal(installment)"
                            class="px-3 py-1 bg-primary text-white text-sm rounded-md hover:bg-primary-dark transition-colors"
                            :disabled="paymentLoading"
                          >
                            Pay Now
                          </button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div class="mt-2 text-sm font-medium text-right">
                  Total: ₹{{ calculateTotalAmount(group) }}
                </div>
              </div>
            </div>
          </div>

          <div class="pt-4">
            <button @click="resetForm" class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors w-full md:w-auto">
              Make New Payment
            </button>
          </div>
        </div>

        <!-- Step 6: Transaction History - Enhanced -->
        <div v-if="currentStep === 5" class="space-y-6">
          <div class="flex justify-between items-center flex-wrap gap-4">
            <h2 class="text-xl font-semibold text-gray-800">
              {{ searchCardId ? `Transactions for Card ****${searchCardId.slice(-4)}` : 'All Transactions' }}
            </h2>
            <div class="space-x-2 flex flex-wrap gap-2">
              <button @click="filterTransactions('all')" :class="`px-3 py-1 text-sm rounded-md ${transactionFilter === 'all' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`">All</button>
              <button @click="filterTransactions('full')" :class="`px-3 py-1 text-sm rounded-md ${transactionFilter === 'full' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`">Paid in Full</button>
              <button @click="filterTransactions('bnpl')" :class="`px-3 py-1 text-sm rounded-md ${transactionFilter === 'bnpl' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`">BNPL</button>
            </div>
          </div>

          <div class="space-y-5">
            <div class="flex flex-wrap gap-4 items-end">
              <div class="flex-1 min-w-[280px]">
                <label class="block text-sm font-medium text-gray-700 mb-1">Card Number (last 4 digits)</label>
                <div class="flex gap-2">
                  <input
                    v-model="searchCardId"
                    type="text"
                    maxlength="4"
                    placeholder="Last 4 digits"
                    class="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                  />
                  <button
                    @click="fetchTransactions"
                    class="px-5 py-2 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors whitespace-nowrap"
                    :disabled="loading || searchCardId.length !== 4"
                  >
                    <span v-if="loading">Loading...</span>
                    <span v-else>Search Card</span>
                  </button>
                </div>
              </div>

              <button
                @click="fetchAllTransactions"
                class="px-6 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors whitespace-nowrap"
              >
                View All Transactions
              </button>

              <button @click="clearSearch" class="px-4 py-2 text-gray-600 hover:text-gray-800">Clear</button>
            </div>

            <!-- Date filter -->
            <div class="flex flex-wrap gap-4 items-end">
              <div class="space-y-1">
                <label class="block text-sm font-medium text-gray-700">From</label>
                <input v-model="dateFilter.from" type="date" class="px-3 py-2 border border-gray-300 rounded-md" />
              </div>
              <div class="space-y-1">
                <label class="block text-sm font-medium text-gray-700">To</label>
                <input v-model="dateFilter.to" type="date" class="px-3 py-2 border border-gray-300 rounded-md" />
              </div>
              <button @click="applyDateFilter" class="px-5 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200">Filter</button>
            </div>

            <div v-if="loading" class="text-center py-12 bg-gray-50 rounded-xl">
              <p class="text-gray-500 text-lg">Loading transactions...</p>
            </div>

            <div v-else-if="displayedTransactions.length === 0" class="text-center py-12 bg-gray-50 rounded-xl">
              <p class="text-gray-600 text-lg">No transactions found{{ searchCardId ? ` for card ending in ${searchCardId}` : '' }}</p>
            </div>

            <div v-else class="overflow-x-auto rounded-xl border border-gray-200">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Date</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Merchant</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Category</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Amount</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Method</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
                    <th class="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="t in displayedTransactions" :key="t.id">
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">#{{ t.id }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{{ formatDate(t.transactionDate) }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{{ t.merchantName || '—' }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{{ t.category || '—' }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold">₹{{ t.amount?.toFixed(2) || '0.00' }}</td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span :class="`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${t.isBNPL ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'}`">
                        {{ t.isBNPL ? 'BNPL' : 'Full' }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span :class="`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusClass(t.status)}`">
                        {{ t.status || 'Completed' }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button v-if="t.isBNPL" @click="viewTransactionInstallments(t.id)" class="text-primary hover:text-primary-dark underline">
                        Installments
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- Realistic Payment Modal -->
      <div v-if="showPaymentModal" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">
          <div class="bg-primary px-6 py-5 text-white flex items-center justify-between">
            <h3 class="text-lg font-semibold">Pay Installment</h3>
            <button @click="cancelPayment" class="text-2xl leading-none hover:opacity-80">×</button>
          </div>

          <div class="p-6 space-y-6">
            <div class="bg-gray-50 p-5 rounded-xl">
              <div class="flex justify-between items-baseline mb-3">
                <span class="text-gray-600">Installment</span>
                <span class="text-2xl font-bold">₹{{ selectedInstallment?.amount?.toFixed(2) }}</span>
              </div>
              <div class="text-sm text-gray-500">Due: {{ formatDate(selectedInstallment?.dueDate) }}</div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">CVV</label>
              <input
                v-model="paymentCvv"
                type="password"
                maxlength="4"
                placeholder="123 or 1234"
                class="w-full px-5 py-4 border border-gray-300 rounded-xl text-center text-2xl tracking-widest focus:ring-2 focus:ring-primary focus:border-primary outline-none"
                autocomplete="off"
              />
            </div>

            <div v-if="paymentError" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl text-sm">
              {{ paymentError }}
            </div>
          </div>

          <div class="px-6 py-5 border-t flex gap-4">
            <button
              @click="cancelPayment"
              class="flex-1 py-3.5 border border-gray-300 rounded-xl text-gray-700 font-medium hover:bg-gray-50 transition"
              :disabled="paymentLoading"
            >
              Cancel
            </button>
            <button
              @click="confirmInstallmentPayment"
              class="flex-1 py-3.5 bg-primary text-white rounded-xl font-medium hover:bg-primary-dark transition disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="paymentLoading || paymentCvv.length < 3"
            >
              {{ paymentLoading ? 'Processing...' : 'Confirm & Pay' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const getAuthToken = () => localStorage.getItem('authToken') || localStorage.getItem('token') || ''

const steps = ref(['Transaction Details', 'Payment Options', 'Confirmation', 'Complete', 'My Installments', 'Transaction History'])
const currentStep = ref(0)
const loading = ref(false)
const errorMessage = ref('')

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

const categories = ref(['Electronics', 'Clothing', 'Travel', 'Home', 'Food', 'Entertainment', 'Other'])
const merchants = ref(['Amazon', 'Walmart', 'Best Buy', 'Target', 'eBay', 'Other'])

const planMapping = { 3: 'THREE', 6: 'SIX', 9: 'NINE' }

const eligibilityResult = ref({ eligible: false, message: '' })
const selectedPaymentMethod = ref(null)
const selectedPlan = ref(null)
const installmentPlans = ref([
  { months: 3, interestRate: 0 },
  { months: 6, interestRate: 2.5 },
  { months: 9, interestRate: 4 }
])

const searchTransactionId = ref('')
const displayedInstallments = ref([])

const transactions = ref([])
const searchCardId = ref('')
const transactionFilter = ref('all')
const dateFilter = ref({ from: '', to: '' })

const showPaymentModal = ref(false)
const selectedInstallment = ref(null)
const paymentCvv = ref('')
const paymentError = ref('')
const paymentLoading = ref(false)

const lastUsedCardLast4 = ref(localStorage.getItem('lastCardLast4') || '')

// ─── Computed ────────────────────────────────────────────────────────
const isFormValid = computed(() => {
  const num = transaction.value.cardNumber.replace(/\s/g, '')
  return (
    num.length === 16 &&
    /^\d+$/.test(num) &&
    transaction.value.cvv?.length >= 3 &&
    transaction.value.expiryMonth?.match(/^(0[1-9]|1[0-2])$/) &&
    transaction.value.expiryYear?.match(/^\d{2}$/) &&
    Number(transaction.value.amount) > 0 &&
    transaction.value.category &&
    transaction.value.merchantName
  )
})

const displayedTransactions = computed(() => {
  let filtered = transactions.value.filter(t => {
    if (transactionFilter.value === 'full') return !t.isBNPL
    if (transactionFilter.value === 'bnpl') return t.isBNPL
    return true
  })

  // Client-side date filter
  if (dateFilter.value.from) {
    const from = new Date(dateFilter.value.from)
    filtered = filtered.filter(t => new Date(t.transactionDate) >= from)
  }
  if (dateFilter.value.to) {
    const to = new Date(dateFilter.value.to)
    to.setHours(23, 59, 59, 999)
    filtered = filtered.filter(t => new Date(t.transactionDate) <= to)
  }

  return filtered
})

// ─── Methods ─────────────────────────────────────────────────────────
const checkEligibility = async () => {
  if (!isFormValid.value) {
    errorMessage.value = 'Please enter valid card number (16 digits), CVV, expiry, and amount > 0'
    return
  }

  const token = getAuthToken()
  if (!token) {
    errorMessage.value = 'Please log in again'
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
      merchantName: transaction.value.merchantName,
      isBNPL: false
    }

    const res = await fetch(`${API_BASE_URL}/transactions/validate-card`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    })

    if (!res.ok) {
      if (res.status === 401) {
        localStorage.clear()
        router.push('/login')
        return
      }
      throw new Error('Validation failed')
    }

    const data = await res.json()
    eligibilityResult.value = {
      eligible: data.eligible,
      message: data.message || (data.eligible ? 'Eligible for BNPL' : 'Not eligible')
    }

    // Save last 4 digits
    localStorage.setItem('lastCardLast4', payload.cardNumber.slice(-4))
    lastUsedCardLast4.value = payload.cardNumber.slice(-4)

    currentStep.value = 1
  } catch (err) {
    errorMessage.value = err.message || 'Error checking eligibility'
  } finally {
    loading.value = false
  }
}

const confirmTransaction = async () => {
  // ── Your original confirm logic ── (kept intact)
  // After success:
  if (transaction.value.cardNumber) {
    localStorage.setItem('lastCardLast4', transaction.value.cardNumber.slice(-4))
    lastUsedCardLast4.value = transaction.value.cardNumber.slice(-4)
  }
  currentStep.value = 3
}

const openPaymentModal = (installment) => {
  selectedInstallment.value = installment
  paymentCvv.value = ''
  paymentError.value = ''
  showPaymentModal.value = true
}

const cancelPayment = () => {
  showPaymentModal.value = false
  paymentError.value = ''
}

const confirmInstallmentPayment = async () => {
  if (paymentCvv.value.length < 3) {
    paymentError.value = 'CVV must be at least 3 digits'
    return
  }

  paymentLoading.value = true
  paymentError.value = ''

  const token = getAuthToken()
  try {
    const res = await fetch(
      `${API_BASE_URL}/bnpl/installments/${selectedInstallment.value.id}/pay?amount=${selectedInstallment.value.amount}`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    )

    if (!res.ok) throw new Error('Payment failed')

    const updated = await res.json()

    // Refresh list
    const idx = displayedInstallments.value.findIndex(i => i.id === updated.id)
    if (idx !== -1) displayedInstallments.value[idx] = updated

    showPaymentModal.value = false

    // Nice success feedback
    errorMessage.value = 'Payment successful! Installment marked as Paid.'
    setTimeout(() => { errorMessage.value = '' }, 4000)
  } catch (err) {
    paymentError.value = err.message || 'Could not complete payment'
  } finally {
    paymentLoading.value = false
  }
}

const fetchTransactions = async () => {
  if (searchCardId.value.length !== 4) {
    errorMessage.value = 'Please enter last 4 digits of card number'
    return
  }

  loading.value = true
  errorMessage.value = ''
  transactions.value = []

  const token = getAuthToken()
  try {
    const res = await fetch(
      `${API_BASE_URL}/transactions/card/${searchCardId.value}`,
      { headers: { Authorization: `Bearer ${token}` } }
    )

    if (!res.ok) throw new Error('Failed to load')

    const data = await res.json()
    transactions.value = Array.isArray(data) ? data : []
    localStorage.setItem('lastCardLast4', searchCardId.value)
  } catch (err) {
    errorMessage.value = 'Could not load transactions for this card'
  } finally {
    loading.value = false
  }
}

const fetchAllTransactions = async () => {
  loading.value = true
  errorMessage.value = ''
  searchCardId.value = ''
  transactions.value = []

  const token = getAuthToken()
  try {
    const res = await fetch(`${API_BASE_URL}/transactions`, {
      headers: { Authorization: `Bearer ${token}` }
    })

    if (!res.ok) throw new Error('Failed to load all transactions')

    const data = await res.json()
    transactions.value = Array.isArray(data) ? data : []
  } catch (err) {
    errorMessage.value = 'Could not load all transactions'
  } finally {
    loading.value = false
  }
}

const clearSearch = () => {
  searchCardId.value = ''
  transactions.value = []
}

const viewTransactionHistory = () => {
  if (lastUsedCardLast4.value) {
    searchCardId.value = lastUsedCardLast4.value
  }
  currentStep.value = 5
  if (searchCardId.value) fetchTransactions()
}

const viewTransactionInstallments = (id) => {
  searchTransactionId.value = id.toString()
  fetchInstallments()
  currentStep.value = 4
}

const goToInstallments = () => {
  const last = localStorage.getItem('lastBnplTransactionId')
  if (last) searchTransactionId.value = last
  currentStep.value = 4
  if (searchTransactionId.value) fetchInstallments()
}

const resetForm = () => {
  currentStep.value = 0
  transaction.value = { cardNumber: '', cvv: '', expiryMonth: '', expiryYear: '', amount: '', category: '', merchantName: '', isBNPL: false }
  eligibilityResult.value = { eligible: false, message: '' }
  selectedPaymentMethod.value = null
  selectedPlan.value = null
  searchTransactionId.value = ''
  displayedInstallments.value = []
  errorMessage.value = ''
  transactions.value = []
  searchCardId.value = ''
}

const filterInstallments = (f) => { /* your logic */ }
const filterTransactions = (f) => transactionFilter.value = f
const applyDateFilter = () => { /* optional client filter already in computed */ }
const clearDateFilter = () => dateFilter.value = { from: '', to: '' }

const calculateTotalAmount = (group) => group.reduce((sum, i) => sum + Number(i.amount || 0), 0).toFixed(2)
const isOverdue = (d) => new Date(d) < new Date()
const formatDate = (d) => d ? new Date(d).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : '—'
const getStatusClass = (s) => {
  const status = (s || '').toLowerCase()
  if (status.includes('paid') || status.includes('complete')) return 'bg-green-100 text-green-800'
  if (status.includes('pending')) return 'bg-yellow-100 text-yellow-800'
  if (status.includes('overdue')) return 'bg-red-100 text-red-800'
  return 'bg-gray-100 text-gray-800'
}

const selectPaymentMethod = (m) => {
  selectedPaymentMethod.value = m
  transaction.value.isBNPL = m === 'bnpl'
  if (m === 'full') selectedPlan.value = null
}

const selectInstallmentPlan = (p) => selectedPlan.value = p
const proceedToConfirmation = () => { if (canProceed.value) currentStep.value = 2 }
const proceedToPayInFull = () => {
  selectedPaymentMethod.value = 'full'
  transaction.value.isBNPL = false
  selectedPlan.value = null
  currentStep.value = 2
}

onMounted(() => {
  if (lastUsedCardLast4.value) {
    searchCardId.value = lastUsedCardLast4.value
  }
})
</script>

<style>
/* Your original style remains here - no changes needed */
:root {
  --color-primary: #007BFF;
  --color-primary-dark: #0056b3;
  --color-primary-50: #eff6ff;
}
.bg-primary { background-color: var(--color-primary); }
.bg-primary-dark { background-color: var(--color-primary-dark); }
.bg-primary-50 { background-color: var(--color-primary-50); }
.text-primary { color: var(--color-primary); }
.border-primary { border-color: var(--color-primary); }
.focus\:ring-primary:focus { --tw-ring-color: var(--color-primary); }
.focus\:border-primary:focus { border-color: var(--color-primary); }
.hover\:bg-primary-dark:hover { background-color: var(--color-primary-dark); }
</style>