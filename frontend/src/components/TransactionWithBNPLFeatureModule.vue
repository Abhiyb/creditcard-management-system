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
              :aria-label="`Step ${index + 1}: ${step}`"
            >
              {{ index + 1 }}
            </div>
            <div class="text-xs mt-2 text-center" :class="currentStep >= index ? 'text-primary' : 'text-gray-500'">
              {{ step }}
            </div>
            <div class="hidden md:block h-1 bg-gray-200 mt-4" :class="index === steps.length - 1 ? 'w-full' : 'w-full'">
              <div
                class="h-full bg-primary transition-all duration-300"
                :style="`width: ${currentStep > index ? '100%' : '0%'}`"
              ></div>
            </div>
            <div v-if="index < steps.length - 1" class="block md:hidden w-8 h-1 bg-gray-200 mt-2">
              <div
                class="h-full bg-primary transition-all duration-300"
                :style="`width: ${currentStep > index ? '100%' : '0%'}`"
              ></div>
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
              <select
                v-model="transaction.category"
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                required
              >
                <option value="" disabled>Select a category</option>
                <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
              </select>
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">Merchant</label>
              <select
                v-model="transaction.merchantName"
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                required
              >
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
                <div class="mt-1 text-sm text-green-700">
                  You're eligible to split this payment into installments.
                </div>
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
                  <div v-else class="mt-2 text-xs text-green-600 font-medium">
                    Interest-free
                  </div>
                </div>
              </div>
            </div>

            <div class="pt-4 flex justify-between">
              <button
                @click="currentStep = 0"
                class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
              >
                Back
              </button>
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
              <button
                @click="currentStep = 0"
                class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
              >
                Back
              </button>
              <button
                @click="proceedToPayInFull"
                class="px-6 py-3 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
              >
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
            <button
              @click="currentStep = 1"
              class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
            >
              Back
            </button>
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
            <span v-if="selectedPaymentMethod === 'bnpl'">
              Your payment has been split into {{ selectedPlan.months }} installments.
            </span>
            <span v-else>
              Your payment has been processed successfully.
            </span>
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
            <button
              @click="viewTransactionHistory"
              class="px-6 py-3 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
            >
              View Transaction History
            </button>
            <button
              @click="resetForm"
              class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
            >
              Make Another Payment
            </button>
          </div>
        </div>

        <!-- Step 5: Installment Management View -->
        <div v-if="currentStep === 4" class="space-y-6">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-semibold text-gray-800">My Installments</h2>
            <div class="space-x-2">
              <button
                @click="filterInstallments('all')"
                :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'all' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >
                All
              </button>
              <button
                @click="filterInstallments('pending')"
                :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'pending' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >
                Pending
              </button>
              <button
                @click="filterInstallments('paid')"
                :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'paid' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >
                Paid
              </button>
              <button
                @click="filterInstallments('overdue')"
                :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'overdue' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >
                Overdue
              </button>
            </div>
          </div>

          <div class="space-y-4">
            <div class="flex justify-between items-center">
              <div class="space-y-1">
                <h3 class="font-medium">Search Installments</h3>
                <div class="flex space-x-2">
                  <input
                    v-model="searchTransactionId"
                    type="text"
                    placeholder="Enter Transaction ID (auto-filled after BNPL payment)"
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
                <button
                  @click="viewTransactionHistory"
                  class="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
                >
                  View Transactions
                </button>
                <button
                  @click="resetForm"
                  class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
                >
                  New Payment
                </button>
              </div>
            </div>

            <div v-if="loading" class="text-center py-8 bg-gray-50 rounded-lg">
              <p class="text-gray-500">Loading installments...</p>
            </div>

            <div v-else-if="displayedInstallments.length === 0" class="text-center py-8 bg-gray-50 rounded-lg">
              <p class="text-gray-500">
                No installments found for this Transaction ID.<br />
                Make sure it's a valid BNPL transaction ID.
              </p>
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
                          <span
                            :class="`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${installment.isPaid ? 'bg-green-100 text-green-800' : isOverdue(installment.dueDate) ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'}`"
                          >
                            {{ installment.isPaid ? 'Paid' : isOverdue(installment.dueDate) ? 'Overdue' : 'Pending' }}
                          </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">₹{{ installment.lateFee?.toFixed(2) || '0.00' }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <button
                            v-if="!installment.isPaid"
                            @click="payInstallment(installment)"
                            class="px-3 py-1 bg-primary text-white text-sm rounded-md hover:bg-primary-dark transition-colors"
                            :disabled="loading"
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
            <button
              @click="resetForm"
              class="px-6 py-3 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors w-full md:w-auto"
            >
              Make New Payment
            </button>
          </div>
        </div>

        <!-- Step 6: Transaction History View -->
        <div v-if="currentStep === 5" class="space-y-6">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-semibold text-gray-800">Transaction History</h2>
            <div class="space-x-2">
              <button
                @click="filterTransactions('all')"
                :class="`px-3 py-1 text-sm rounded-md ${transactionFilter === 'all' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >
                All
              </button>
              <button
                @click="filterTransactions('full')"
                :class="`px-3 py-1 text-sm rounded-md ${transactionFilter === 'full' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >
                Paid in Full
              </button>
              <button
                @click="filterTransactions('bnpl')"
                :class="`px-3 py-1 text-sm rounded-md ${transactionFilter === 'bnpl' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >
                BNPL
              </button>
            </div>
          </div>

          <div class="space-y-4">
            <div class="flex justify-between items-center flex-wrap gap-4">
              <div class="space-y-1 flex-1 min-w-[280px]">
                <h3 class="font-medium">Search Transactions</h3>
                <div class="flex space-x-2">
                  <input
                    v-model="searchCardId"
                    type="text"
                    placeholder="Enter Card ID (numeric - not last 4 digits)"
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                  />
                  <button
                    @click="fetchTransactions"
                    class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
                    :disabled="loading || !searchCardId"
                  >
                    <span v-if="loading">Loading...</span>
                    <span v-else>Search</span>
                  </button>
                </div>
              </div>
              <div class="flex space-x-2">
                <button
                  @click="goToInstallments"
                  class="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
                >
                  View Installments
                </button>
                <button
                  @click="resetForm"
                  class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary-dark transition-colors"
                >
                  New Payment
                </button>
              </div>
            </div>

            <!-- Date range filter -->
            <div class="flex flex-wrap gap-4 items-end">
              <div class="space-y-1">
                <label class="block text-sm font-medium text-gray-700">From Date</label>
                <input
                  v-model="dateFilter.from"
                  type="date"
                  class="px-3 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                />
              </div>
              <div class="space-y-1">
                <label class="block text-sm font-medium text-gray-700">To Date</label>
                <input
                  v-model="dateFilter.to"
                  type="date"
                  class="px-3 py-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
                />
              </div>
              <button
                @click="applyDateFilter"
                class="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors"
              >
                Apply Filter
              </button>
              <button
                @click="clearDateFilter"
                class="px-4 py-2 text-gray-500 rounded-md hover:text-gray-700 transition-colors"
              >
                Clear
              </button>
            </div>

            <div v-if="loading" class="text-center py-8 bg-gray-50 rounded-lg">
              <p class="text-gray-500">Loading transactions...</p>
            </div>

            <div v-else-if="transactions.length === 0" class="text-center py-8 bg-gray-50 rounded-lg">
              <p class="text-gray-500">No transactions found for this card ID</p>
            </div>

            <div v-else class="overflow-x-auto">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Transaction ID</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Merchant</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Category</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Payment Method</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                    <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="transaction in transactions" :key="transaction.id">
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">#{{ transaction.id }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(transaction.transactionDate) }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ transaction.merchantName || 'N/A' }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ transaction.category || 'N/A' }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">₹{{ transaction.amount?.toFixed(2) || '0.00' }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <span
                        :class="`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${transaction.isBNPL ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'}`"
                      >
                        {{ transaction.isBNPL ? 'BNPL' : 'Paid in Full' }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <span
                        :class="`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusClass(transaction.status)}`"
                      >
                        {{ transaction.status || 'Unknown' }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button
                        v-if="transaction.isBNPL"
                        @click="viewTransactionInstallments(transaction.id)"
                        class="text-primary hover:text-primary-dark"
                      >
                        View Installments
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- Pagination (kept your original style) -->
              <div v-if="transactions.length > 0" class="flex items-center justify-between border-t border-gray-200 px-4 py-3 sm:px-6 mt-4">
                <div class="flex-1 flex justify-between sm:hidden">
                  <button
                    @click="prevPage"
                    :disabled="currentPage === 1"
                    class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                  >
                    Previous
                  </button>
                  <button
                    @click="nextPage"
                    :disabled="currentPage === totalPages"
                    class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                  >
                    Next
                  </button>
                </div>
                <div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
                  <div>
                    <p class="text-sm text-gray-700">
                      Showing
                      <span class="font-medium">{{ (currentPage - 1) * pageSize + 1 }}</span>
                      to
                      <span class="font-medium">{{ Math.min(currentPage * pageSize, totalTransactions) }}</span>
                      of
                      <span class="font-medium">{{ totalTransactions }}</span>
                      results
                    </p>
                  </div>
                  <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                    <button
                      @click="prevPage"
                      :disabled="currentPage === 1"
                      class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                    >
                      <span class="sr-only">Previous</span>
                      <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
                      </svg>
                    </button>
                    <button
                      v-for="page in paginationRange"
                      :key="page"
                      @click="goToPage(page)"
                      :class="`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${currentPage === page ? 'z-10 bg-primary border-primary text-white' : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'}`"
                    >
                      {{ page }}
                    </button>
                    <button
                      @click="nextPage"
                      :disabled="currentPage === totalPages"
                      class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                    >
                      <span class="sr-only">Next</span>
                      <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                      </svg>
                    </button>
                  </nav>
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
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const getAuthToken = () => localStorage.getItem('authToken') || localStorage.getItem('token') || '';

const steps = ref(['Transaction Details', 'Payment Options', 'Confirmation', 'Complete', 'My Installments', 'Transaction History']);
const currentStep = ref(0);
const loading = ref(false);
const errorMessage = ref('');

const transaction = ref({
  cardNumber: '',
  cvv: '',
  expiryMonth: '',
  expiryYear: '',
  amount: '',
  category: '',
  merchantName: '',
  isBNPL: false
});

const categories = ref(['Electronics', 'Clothing', 'Travel', 'Home', 'Food', 'Entertainment', 'Other']);
const merchants = ref(['Amazon', 'Walmart', 'Best Buy', 'Target', 'eBay', 'Other']);

const planMapping = { 3: 'THREE', 6: 'SIX', 9: 'NINE' };

const eligibilityResult = ref({ eligible: false, message: '' });
const selectedPaymentMethod = ref(null);
const selectedPlan = ref(null);
const installmentPlans = ref([
  { months: 3, interestRate: 0 },
  { months: 6, interestRate: 2.5 },
  { months: 9, interestRate: 4 }
]);

const installments = ref([]);
const searchTransactionId = ref('');
const installmentFilter = ref('all');
const displayedInstallments = ref([]);

const transactions = ref([]);
const searchCardId = ref('');
const transactionFilter = ref('all');
const dateFilter = ref({ from: '', to: '' });
const currentPage = ref(1);
const pageSize = 10;
const totalTransactions = ref(0);

const totalPages = computed(() => Math.ceil(totalTransactions.value / pageSize));
const paginationRange = computed(() => {
  const maxPages = 5;
  const range = [];
  const half = Math.floor(maxPages / 2);
  let start = Math.max(1, currentPage.value - half);
  let end = Math.min(totalPages.value, start + maxPages - 1);
  if (end - start + 1 < maxPages) start = Math.max(1, end - maxPages + 1);
  for (let i = start; i <= end; i++) range.push(i);
  return range;
});

const groupedDisplayedInstallments = computed(() => {
  const groups = {};
  displayedInstallments.value.forEach(i => {
    const transId = i.transactionId || searchTransactionId.value || 'Unknown';
    if (!groups[transId]) groups[transId] = [];
    groups[transId].push(i);
  });
  return groups;
});

const canProceed = computed(() => {
  return selectedPaymentMethod.value === 'full' || (selectedPaymentMethod.value === 'bnpl' && selectedPlan.value);
});

const calculatedInstallments = computed(() => {
  if (!selectedPlan.value) return [];
  const amount = parseFloat(transaction.value.amount);
  const months = selectedPlan.value.months;
  const installmentAmount = (amount / months).toFixed(2);
  const today = new Date();
  const arr = [];
  for (let i = 0; i < months; i++) {
    const due = new Date(today);
    due.setMonth(today.getMonth() + i + 1);
    arr.push({
      amount: installmentAmount,
      dueDate: due.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
    });
  }
  return arr;
});

const isFormValid = computed(() => {
  const num = transaction.value.cardNumber.replace(/\s/g, '');
  return (
    num.length === 16 &&
    /^\d+$/.test(num) &&
    transaction.value.cvv?.length >= 3 &&
    transaction.value.cvv?.length <= 4 &&
    /^\d+$/.test(transaction.value.cvv || '') &&
    transaction.value.expiryMonth?.match(/^(0[1-9]|1[0-2])$/) &&
    transaction.value.expiryYear?.match(/^\d{2}$/) &&
    Number(transaction.value.amount) > 0 &&
    transaction.value.category &&
    transaction.value.merchantName
  );
});

// ─── Check Eligibility ───────────────────────────────────────────────
const checkEligibility = async () => {
  if (!isFormValid.value) {
    errorMessage.value = 'Please enter valid card number (16 digits), CVV (3-4 digits), expiry (MM/YY), and amount > 0';
    return;
  }

  const token = getAuthToken();
  if (!token) {
    errorMessage.value = 'Please log in again. No active session found.';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const payload = {
      cardNumber: transaction.value.cardNumber.replace(/\s/g, ''),
      cvv: transaction.value.cvv,
      expiryMonth: transaction.value.expiryMonth,
      expiryYear: transaction.value.expiryYear,
      amount: parseFloat(transaction.value.amount),
      category: transaction.value.category,
      merchantName: transaction.value.merchantName,
      isBNPL: false
    };

    const response = await fetch(`${API_BASE_URL}/transactions/validate-card`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      if (response.status === 401) {
        errorMessage.value = 'Session expired. Please log in again.';
        localStorage.clear();
        router.push('/login');
        return;
      }
      throw new Error('Card validation failed');
    }

    const data = await response.json();
    eligibilityResult.value = {
      eligible: data.eligible,
      message: data.message || (data.eligible ? 'Eligible for BNPL' : 'Not eligible')
    };
    currentStep.value = 1;
  } catch (err) {
    errorMessage.value = err.message || 'Error during eligibility check';
  } finally {
    loading.value = false;
  }
};

// ─── Confirm Transaction + Save last BNPL ID ─────────────────────────
const confirmTransaction = async () => {
  loading.value = true;
  errorMessage.value = '';

  const token = getAuthToken();
  if (!token) {
    errorMessage.value = 'Please log in again. No active session found.';
    loading.value = false;
    return;
  }

  const payload = {
    cardNumber: transaction.value.cardNumber.replace(/\s/g, ''),
    cvv: transaction.value.cvv,
    expiryMonth: transaction.value.expiryMonth,
    expiryYear: transaction.value.expiryYear,
    amount: parseFloat(transaction.value.amount),
    category: transaction.value.category,
    merchantName: transaction.value.merchantName,
    isBNPL: transaction.value.isBNPL
  };

  try {
    const endpoint = transaction.value.isBNPL
      ? `${API_BASE_URL}/transactions/bnpl?plan=${planMapping[selectedPlan.value.months]}`
      : `${API_BASE_URL}/transactions`;

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      if (response.status === 401) {
        errorMessage.value = 'Session expired. Please log in again.';
        localStorage.clear();
        router.push('/login');
        return;
      }
      throw new Error(errorData.message || 'Transaction failed');
    }

    const savedTransaction = await response.json();

    // Save last BNPL transaction ID for auto-load
    if (transaction.value.isBNPL || savedTransaction.isBNPL) {
      const txId = savedTransaction.id || savedTransaction.transactionId;
      if (txId) {
        localStorage.setItem('lastBnplTransactionId', txId.toString());
      }
    }

    currentStep.value = 3;
  } catch (error) {
    errorMessage.value = `Error processing transaction: ${error.message}`;
  } finally {
    loading.value = false;
  }
};

// ─── Auto-load last BNPL transaction installments ────────────────────
const goToInstallments = () => {
  const lastId = localStorage.getItem('lastBnplTransactionId');
  if (lastId) {
    searchTransactionId.value = lastId;
  } else {
    errorMessage.value = 'No recent BNPL payment found. Please enter Transaction ID manually.';
  }

  currentStep.value = 4;

  // Auto fetch if we have ID
  if (searchTransactionId.value) {
    fetchInstallments();
  }
};

// ─── Fetch Installments from your real endpoint ─────────────────────
const fetchInstallments = async () => {
  if (!searchTransactionId.value.trim()) {
    errorMessage.value = 'Please enter a valid Transaction ID';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  displayedInstallments.value = [];

  const token = getAuthToken();
  if (!token) {
    errorMessage.value = 'Authentication required. Please login.';
    loading.value = false;
    return;
  }

  try {
    const response = await fetch(
      `${API_BASE_URL}/bnpl/installments/transaction/${searchTransactionId.value}`,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );

    if (!response.ok) {
      if (response.status === 401) {
        errorMessage.value = 'Session expired. Please log in again.';
        localStorage.clear();
        router.push('/login');
        return;
      }
      throw new Error('Failed to load installments');
    }

    const data = await response.json();
    displayedInstallments.value = Array.isArray(data) ? data : [];
  } catch (err) {
    errorMessage.value = err.message || 'Could not load installments. Check Transaction ID.';
  } finally {
    loading.value = false;
  }
};

// ─── Fetch Transaction History from your real endpoint ──────────────
const fetchTransactions = async () => {
  if (!searchCardId.value.trim()) {
    errorMessage.value = 'Please enter Card ID (numeric)';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  transactions.value = [];

  const token = getAuthToken();
  if (!token) {
    errorMessage.value = 'Authentication required. Please login.';
    loading.value = false;
    return;
  }

  try {
    const response = await fetch(
      `${API_BASE_URL}/transactions/card/${searchCardId.value}`,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );

    if (!response.ok) {
      if (response.status === 401) {
        errorMessage.value = 'Session expired. Please log in again.';
        localStorage.clear();
        router.push('/login');
        return;
      }
      throw new Error('Failed to load transactions');
    }

    const data = await response.json();
    transactions.value = Array.isArray(data) ? data : [];
    totalTransactions.value = data.length || 0;
  } catch (err) {
    errorMessage.value = err.message || 'Could not load transaction history. Check Card ID.';
  } finally {
    loading.value = false;
  }
};

const viewTransactionHistory = () => {
  // Optional: pre-fill if possible (you may need to store cardId somewhere)
  currentStep.value = 5;
  fetchTransactions();
};

const viewTransactionInstallments = (id) => {
  searchTransactionId.value = id.toString();
  fetchInstallments();
  currentStep.value = 4;
};

const resetForm = () => {
  currentStep.value = 0;
  transaction.value = {
    cardNumber: '',
    cvv: '',
    expiryMonth: '',
    expiryYear: '',
    amount: '',
    category: '',
    merchantName: '',
    isBNPL: false
  };
  eligibilityResult.value = { eligible: false, message: '' };
  selectedPaymentMethod.value = null;
  selectedPlan.value = null;
  searchTransactionId.value = '';
  displayedInstallments.value = [];
  errorMessage.value = '';
  transactions.value = [];
  searchCardId.value = '';
  // Keep lastBnplTransactionId so auto-load still works next time
  // localStorage.removeItem('lastBnplTransactionId'); // ← uncomment if you want full reset
};

// Placeholder functions (you can expand them later)
const filterInstallments = (filter) => { installmentFilter.value = filter; };
const filterTransactions = (filter) => { transactionFilter.value = filter; };
const applyDateFilter = () => { /* add date filtering logic if needed */ };
const clearDateFilter = () => { dateFilter.value = { from: '', to: '' }; };
const prevPage = () => { if (currentPage.value > 1) currentPage.value--; };
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++; };
const goToPage = (page) => { currentPage.value = page; };
const payInstallment = async (installment) => {
  // Implement payment logic here (call your /pay endpoint)
  alert(`Paying installment ${installment.id} - to be implemented`);
};
const calculateTotalAmount = (group) => group.reduce((sum, i) => sum + (Number(i.amount) || 0), 0).toFixed(2);
const isOverdue = (dueDate) => new Date(dueDate) < new Date();
const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
};
const getStatusClass = (status) => {
  if (!status) return 'bg-gray-100 text-gray-800';
  if (status.toLowerCase().includes('paid')) return 'bg-green-100 text-green-800';
  if (status.toLowerCase().includes('pending')) return 'bg-yellow-100 text-yellow-800';
  if (status.toLowerCase().includes('overdue')) return 'bg-red-100 text-red-800';
  return 'bg-gray-100 text-gray-800';
};

const selectPaymentMethod = (method) => {
  selectedPaymentMethod.value = method;
  transaction.value.isBNPL = method === 'bnpl';
  if (method === 'full') selectedPlan.value = null;
};

const selectInstallmentPlan = (plan) => {
  selectedPlan.value = plan;
};

const proceedToConfirmation = () => {
  if (canProceed.value) currentStep.value = 2;
};

const proceedToPayInFull = () => {
  selectedPaymentMethod.value = 'full';
  transaction.value.isBNPL = false;
  selectedPlan.value = null;
  currentStep.value = 2;
};

onMounted(() => {
  // Optional: pre-fill from recent if you store it
});
</script>

<style>
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