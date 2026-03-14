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
              <div
                class="h-full bg-primary transition-all duration-300"
                :style="`width: ${currentStep > index ? '100%' : '0%'}`"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Main content -->
      <div class="p-6">
        <div v-if="errorMessage" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6 text-sm text-red-700">
          {{ errorMessage }}
        </div>

        <!-- Step 1: Transaction Details -->
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

        <!-- Step 2: Eligibility & Payment Options -->
        <div v-if="currentStep === 1" class="space-y-6">
          <div v-if="eligibilityResult.eligible">
            <div class="bg-green-50 border border-green-200 rounded-md p-4 flex items-start">
              <svg class="w-5 h-5 text-green-500 mr-3 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM5 9l4 4 6-6" clip-rule="evenodd"/>
              </svg>
              <div>
                <h3 class="font-semibold text-green-800">Eligible for BNPL</h3>
                <p class="text-sm text-green-700 mt-1">You can split this payment into installments.</p>
              </div>
            </div>

            <h2 class="text-xl font-semibold mt-6">Choose Payment Method</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
              <div
                @click="selectPaymentMethod('full')"
                class="border-2 rounded-xl p-5 cursor-pointer transition-all"
                :class="selectedPaymentMethod === 'full' ? 'border-primary bg-primary-50' : 'border-gray-200 hover:border-primary'"
              >
                <div class="flex items-center gap-3">
                  <div class="w-5 h-5 rounded-full border-2 flex items-center justify-center" :class="selectedPaymentMethod === 'full' ? 'border-primary' : 'border-gray-300'">
                    <div v-if="selectedPaymentMethod === 'full'" class="w-3 h-3 rounded-full bg-primary"></div>
                  </div>
                  <div>
                    <h3 class="font-semibold">Pay in Full</h3>
                    <p class="text-sm text-gray-500">Pay now</p>
                  </div>
                </div>
                <div class="mt-4 text-xl font-bold">₹{{ transaction.amount || '0' }}</div>
              </div>

              <div
                @click="selectPaymentMethod('bnpl')"
                class="border-2 rounded-xl p-5 cursor-pointer transition-all"
                :class="selectedPaymentMethod === 'bnpl' ? 'border-primary bg-primary-50' : 'border-gray-200 hover:border-primary'"
              >
                <div class="flex items-center gap-3">
                  <div class="w-5 h-5 rounded-full border-2 flex items-center justify-center" :class="selectedPaymentMethod === 'bnpl' ? 'border-primary' : 'border-gray-300'">
                    <div v-if="selectedPaymentMethod === 'bnpl'" class="w-3 h-3 rounded-full bg-primary"></div>
                  </div>
                  <div>
                    <h3 class="font-semibold">Pay Later (BNPL)</h3>
                    <p class="text-sm text-gray-500">Split into installments</p>
                  </div>
                </div>
                <div class="mt-4 text-sm text-primary font-medium">View plans ↓</div>
              </div>
            </div>

            <div v-if="selectedPaymentMethod === 'bnpl'" class="mt-8">
              <h3 class="font-semibold text-lg mb-4">Select Installment Plan</h3>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div
                  v-for="plan in installmentPlans"
                  :key="plan.months"
                  @click="selectInstallmentPlan(plan)"
                  class="border-2 rounded-xl p-6 text-center cursor-pointer transition-all"
                  :class="selectedPlan?.months === plan.months ? 'border-primary bg-primary-50' : 'border-gray-200 hover:border-primary'"
                >
                  <div class="text-3xl font-bold">{{ plan.months }}</div>
                  <div class="text-sm text-gray-500">months</div>
                  <div class="mt-3 text-xl font-semibold">₹{{ (Number(transaction.amount) / plan.months).toFixed(2) }}</div>
                  <div class="mt-2 text-xs" :class="plan.interestRate > 0 ? 'text-amber-600' : 'text-green-600'">
                    {{ plan.interestRate > 0 ? `${plan.interestRate}% interest` : 'Interest-free' }}
                  </div>
                </div>
              </div>
            </div>

            <div class="mt-8 flex gap-4">
              <button @click="currentStep = 0" class="flex-1 py-3 border border-gray-300 rounded-xl hover:bg-gray-50">
                Back
              </button>
              <button
                @click="proceedToConfirmation"
                :disabled="!canProceed || loading"
                class="flex-1 py-3 bg-primary text-white rounded-xl hover:bg-primary-dark disabled:opacity-60"
              >
                {{ loading ? 'Processing...' : 'Continue' }}
              </button>
            </div>
          </div>

          <div v-else class="space-y-6">
            <div class="bg-yellow-50 border border-yellow-200 rounded-xl p-5 flex items-start gap-4">
              <svg class="w-6 h-6 text-yellow-500 flex-shrink-0 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92z"/>
              </svg>
              <div>
                <h3 class="font-semibold text-yellow-800">Not Eligible for BNPL</h3>
                <p class="text-sm text-yellow-700 mt-1">{{ eligibilityResult.message || 'Not eligible for installments at this time.' }}</p>
              </div>
            </div>
            <div class="flex gap-4">
              <button @click="currentStep = 0" class="flex-1 py-3 border border-gray-300 rounded-xl hover:bg-gray-50">
                Back
              </button>
              <button @click="proceedToPayInFull" class="flex-1 py-3 bg-primary text-white rounded-xl hover:bg-primary-dark">
                Pay in Full
              </button>
            </div>
          </div>
        </div>

        <!-- Step 3: Confirmation -->
        <div v-if="currentStep === 2" class="space-y-6">
          <h2 class="text-xl font-semibold">Confirm Payment</h2>
          <div class="bg-gray-50 rounded-xl p-6 space-y-4 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-600">Card ending</span>
              <span class="font-medium">**** **** **** {{ transaction.cardNumber.slice(-4) || 'XXXX' }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Amount</span>
              <span class="font-medium text-lg">₹{{ transaction.amount }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Merchant</span>
              <span class="font-medium">{{ transaction.merchantName }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Category</span>
              <span class="font-medium">{{ transaction.category }}</span>
            </div>

            <div v-if="selectedPaymentMethod === 'bnpl' && selectedPlan" class="pt-4 border-t">
              <h3 class="font-medium">Plan: {{ selectedPlan.months }} months</h3>
              <div class="mt-3 space-y-2 text-sm">
                <div v-for="(inst, i) in calculatedInstallments" :key="i" class="flex justify-between">
                  <span>Payment {{ i + 1 }} ({{ inst.dueDate }})</span>
                  <span class="font-medium">₹{{ inst.amount }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="flex gap-4">
            <button @click="currentStep = 1" class="flex-1 py-3 border border-gray-300 rounded-xl hover:bg-gray-50">
              Back
            </button>
            <button
              @click="confirmTransaction"
              :disabled="loading"
              class="flex-1 py-3 bg-primary text-white rounded-xl hover:bg-primary-dark disabled:opacity-60"
            >
              {{ loading ? 'Processing...' : 'Confirm Payment' }}
            </button>
          </div>
        </div>

        <!-- Step 4: Success -->
        <div v-if="currentStep === 3" class="text-center py-10 space-y-6">
          <div class="mx-auto w-20 h-20 bg-green-100 rounded-full flex items-center justify-center">
            <svg class="w-10 h-10 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/>
            </svg>
          </div>
          <h2 class="text-3xl font-bold">Payment Successful!</h2>
          <p class="text-lg text-gray-600">
            <span v-if="selectedPaymentMethod === 'bnpl'">
              Split into {{ selectedPlan?.months }} installments.
            </span>
            <span v-else>Processed successfully.</span>
          </p>
          <div class="flex flex-col sm:flex-row gap-4 justify-center">
            <button
              v-if="selectedPaymentMethod === 'bnpl'"
              @click="goToInstallments"
              class="px-8 py-4 bg-primary text-white rounded-xl hover:bg-primary-dark font-semibold"
              :disabled="loading"
            >
              {{ loading ? 'Loading...' : 'View My Installments' }}
            </button>
            <button
              @click="viewTransactionHistory"
              class="px-8 py-4 bg-primary text-white rounded-xl hover:bg-primary-dark font-semibold"
            >
              View History
            </button>
            <button
              @click="resetForm"
              class="px-8 py-4 border-2 border-gray-300 rounded-xl hover:bg-gray-50 font-medium"
            >
              New Payment
            </button>
          </div>
        </div>

        <!-- Step 5: My Installments -->
        <div v-if="currentStep === 4" class="space-y-6">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-semibold text-gray-800">My Installments</h2>
            <div class="space-x-2">
              <button
                @click="filterInstallments('all')"
                :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'all' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >All</button>
              <button
                @click="filterInstallments('pending')"
                :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'pending' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >Pending</button>
              <button
                @click="filterInstallments('paid')"
                :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'paid' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >Paid</button>
              <button
                @click="filterInstallments('overdue')"
                :class="`px-3 py-1 text-sm rounded-md ${installmentFilter === 'overdue' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >Overdue</button>
            </div>
          </div>

          <div class="space-y-4">
            <div class="flex gap-3">
              <input
                v-model="searchTransactionId"
                type="text"
                placeholder="Transaction ID (auto-filled after BNPL payment)"
                class="flex-1 px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary"
              />
              <button
                @click="fetchInstallments"
                :disabled="loading || !searchTransactionId"
                class="px-6 py-3 bg-primary text-white rounded-xl hover:bg-primary-dark disabled:opacity-60"
              >
                {{ loading ? 'Loading...' : 'Load' }}
              </button>
            </div>

            <div v-if="loading" class="text-center py-12 bg-gray-50 rounded-xl">Loading installments...</div>

            <div v-else-if="displayedInstallments.length === 0" class="text-center py-12 bg-gray-50 rounded-xl text-gray-500">
              No installments found for this Transaction ID.<br />
              Make sure it's a valid BNPL transaction ID.
            </div>

            <div v-else class="space-y-6">
              <div v-for="(group, transId) in groupedDisplayedInstallments" :key="transId" class="border rounded-xl p-5">
                <h3 class="font-semibold mb-4">Transaction #{{ transId }}</h3>
                <div class="overflow-x-auto">
                  <table class="min-w-full text-sm">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="px-4 py-3 text-left">ID</th>
                        <th class="px-4 py-3 text-left">No.</th>
                        <th class="px-4 py-3 text-left">Amount</th>
                        <th class="px-4 py-3 text-left">Due Date</th>
                        <th class="px-4 py-3 text-left">Status</th>
                        <th class="px-4 py-3 text-left">Late Fee</th>
                        <th class="px-4 py-3 text-right">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="inst in group" :key="inst.id" class="border-t hover:bg-gray-50">
                        <td class="px-4 py-3">{{ inst.id }}</td>
                        <td class="px-4 py-3">{{ inst.installmentNumber }}</td>
                        <td class="px-4 py-3 font-medium">₹{{ inst.amount?.toFixed(2) }}</td>
                        <td class="px-4 py-3">{{ formatDate(inst.dueDate) }}</td>
                        <td class="px-4 py-3">
                          <span
                            :class="inst.isPaid ? 'bg-green-100 text-green-700' : isOverdue(inst.dueDate) ? 'bg-red-100 text-red-700' : 'bg-yellow-100 text-yellow-700'"
                            class="px-2 py-1 rounded-full text-xs font-medium"
                          >
                            {{ inst.isPaid ? 'Paid' : isOverdue(inst.dueDate) ? 'Overdue' : 'Pending' }}
                          </span>
                        </td>
                        <td class="px-4 py-3">₹{{ inst.lateFee?.toFixed(2) || '0.00' }}</td>
                        <td class="px-4 py-3 text-right">
                          <button
                            v-if="!inst.isPaid"
                            @click="payInstallment(inst)"
                            class="px-4 py-1.5 bg-primary text-white text-sm rounded-lg hover:bg-primary-dark disabled:opacity-60"
                            :disabled="loading"
                          >
                            Pay Now
                          </button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div class="mt-4 text-right font-medium">
                  Total: ₹{{ calculateTotalAmount(group) }}
                </div>
              </div>
            </div>
          </div>

          <button @click="resetForm" class="w-full py-4 border-2 border-gray-300 rounded-xl hover:bg-gray-50">
            Make New Payment
          </button>
        </div>

        <!-- Step 6: Transaction History -->
        <div v-if="currentStep === 5" class="space-y-6">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-semibold text-gray-800">Transaction History</h2>
            <div class="space-x-2">
              <button
                @click="filterTransactions('all')"
                :class="`px-3 py-1 text-sm rounded-md ${transactionFilter === 'all' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >All</button>
              <button
                @click="filterTransactions('full')"
                :class="`px-3 py-1 text-sm rounded-md ${transactionFilter === 'full' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >Paid in Full</button>
              <button
                @click="filterTransactions('bnpl')"
                :class="`px-3 py-1 text-sm rounded-md ${transactionFilter === 'bnpl' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'}`"
              >BNPL</button>
            </div>
          </div>

          <div class="space-y-4">
            <div class="flex gap-3">
              <input
                v-model="searchCardId"
                type="text"
                placeholder="Enter Card ID (numeric)"
                class="flex-1 px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary"
              />
              <button
                @click="fetchTransactions"
                :disabled="loading || !searchCardId"
                class="px-6 py-3 bg-primary text-white rounded-xl hover:bg-primary-dark disabled:opacity-60"
              >
                {{ loading ? 'Loading...' : 'Search' }}
              </button>
            </div>

            <div v-if="loading" class="text-center py-12 bg-gray-50 rounded-xl">Loading transactions...</div>

            <div v-else-if="transactions.length === 0" class="text-center py-12 bg-gray-50 rounded-xl text-gray-500">
              No transactions found for this Card ID.<br />
              Make sure the Card ID is correct.
            </div>

            <div v-else class="overflow-x-auto rounded-xl border">
              <table class="min-w-full divide-y divide-gray-200 text-sm">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-6 py-3 text-left font-medium text-gray-500 uppercase">ID</th>
                    <th class="px-6 py-3 text-left font-medium text-gray-500 uppercase">Date</th>
                    <th class="px-6 py-3 text-left font-medium text-gray-500 uppercase">Merchant</th>
                    <th class="px-6 py-3 text-left font-medium text-gray-500 uppercase">Amount</th>
                    <th class="px-6 py-3 text-left font-medium text-gray-500 uppercase">Method</th>
                    <th class="px-6 py-3 text-left font-medium text-gray-500 uppercase">Status</th>
                    <th class="px-6 py-3 text-right font-medium text-gray-500 uppercase">Actions</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-200">
                  <tr v-for="tx in transactions" :key="tx.id" class="hover:bg-gray-50">
                    <td class="px-6 py-4 font-medium">#{{ tx.id }}</td>
                    <td class="px-6 py-4 text-gray-600">{{ formatDate(tx.transactionDate) }}</td>
                    <td class="px-6 py-4 text-gray-600">{{ tx.merchantName || 'N/A' }}</td>
                    <td class="px-6 py-4 font-medium">₹{{ tx.amount?.toFixed(2) || '0.00' }}</td>
                    <td class="px-6 py-4">
                      <span
                        :class="tx.isBNPL ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'"
                        class="px-2 py-1 rounded-full text-xs font-medium"
                      >
                        {{ tx.isBNPL ? 'BNPL' : 'Full' }}
                      </span>
                    </td>
                    <td class="px-6 py-4">
                      <span
                        :class="getStatusClass(tx.status)"
                        class="px-2 py-1 rounded-full text-xs font-medium"
                      >
                        {{ tx.status || 'Unknown' }}
                      </span>
                    </td>
                    <td class="px-6 py-4 text-right">
                      <button
                        v-if="tx.isBNPL"
                        @click="viewTransactionInstallments(tx.id)"
                        class="text-primary hover:underline font-medium"
                      >
                        View Installments
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
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

const getAuthToken = () => localStorage.getItem('token') || ''

const steps = ref(['Transaction Details', 'Payment Options', 'Confirmation', 'Complete', 'My Installments', 'History'])
const currentStep = ref(0)
const loading = ref(false)
const errorMessage = ref('')

const transaction = ref({
  cardNumber: '',
  cvv: '',
  expiryMonth: '',
  expiryYear: '',
  amount: null,
  category: '',
  merchantName: '',
  isBNPL: false
})

const categories = ref(['Electronics', 'Clothing', 'Travel', 'Home', 'Food', 'Entertainment', 'Other'])
const merchants = ref(['Amazon', 'Walmart', 'Best Buy', 'Target', 'eBay', 'Other'])

const installmentPlans = ref([
  { months: 3, interestRate: 0 },
  { months: 6, interestRate: 2.5 },
  { months: 9, interestRate: 4 }
])

const eligibilityResult = ref({ eligible: false, message: '' })
const selectedPaymentMethod = ref(null)
const selectedPlan = ref(null)

const searchTransactionId = ref('')
const installmentFilter = ref('all')
const displayedInstallments = ref([])

const searchCardId = ref('')
const transactionFilter = ref('all')
const transactions = ref([])

const isFormValid = computed(() => {
  const num = transaction.value.cardNumber?.replace(/\s/g, '') || ''
  return (
    num.length === 16 &&
    /^\d{16}$/.test(num) &&
    (transaction.value.cvv?.length >= 3 && transaction.value.cvv?.length <= 4) &&
    /^\d+$/.test(transaction.value.cvv || '') &&
    transaction.value.expiryMonth?.match(/^(0[1-9]|1[0-2])$/) &&
    transaction.value.expiryYear?.match(/^\d{2}$/) &&
    Number(transaction.value.amount) > 0 &&
    transaction.value.category &&
    transaction.value.merchantName
  )
})

const canProceed = computed(() =>
  selectedPaymentMethod.value === 'full' ||
  (selectedPaymentMethod.value === 'bnpl' && selectedPlan.value)
)

const calculatedInstallments = computed(() => {
  if (!selectedPlan.value) return []
  const amt = Number(transaction.value.amount)
  const months = selectedPlan.value.months
  const per = (amt / months).toFixed(2)
  const today = new Date()
  return Array.from({ length: months }, (_, i) => {
    const due = new Date(today)
    due.setMonth(today.getMonth() + i + 1)
    return {
      amount: per,
      dueDate: due.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
    }
  })
})

// ─── Check Eligibility ───────────────────────────────────────
const checkEligibility = async () => {
  if (!isFormValid.value) {
    errorMessage.value = 'Please fill all fields correctly'
    return
  }

  loading.value = true
  const token = getAuthToken()
  if (!token) {
    errorMessage.value = 'Please login again'
    loading.value = false
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

    if (!res.ok) throw new Error('Validation failed')

    const data = await res.json()
    eligibilityResult.value = {
      eligible: data.eligible === true,
      message: data.message || (data.eligible ? 'Eligible' : 'Not eligible')
    }
    currentStep.value = 1
  } catch (err) {
    errorMessage.value = err.message || 'Failed to validate card'
  } finally {
    loading.value = false
  }
}

// ─── Confirm Transaction + Save BNPL ID ──────────────────────
const confirmTransaction = async () => {
  loading.value = true
  errorMessage.value = ''

  const token = getAuthToken()
  if (!token) {
    errorMessage.value = 'Please login again'
    loading.value = false
    return
  }

  const payload = {
    cardNumber: transaction.value.cardNumber.replace(/\s/g, ''),
    cvv: transaction.value.cvv,
    expiryMonth: transaction.value.expiryMonth,
    expiryYear: transaction.value.expiryYear,
    amount: Number(transaction.value.amount),
    category: transaction.value.category,
    merchantName: transaction.value.merchantName,
    isBNPL: transaction.value.isBNPL
  }

  try {
    let endpoint = `${API_BASE_URL}/transactions`
    if (transaction.value.isBNPL && selectedPlan.value) {
      endpoint = `${API_BASE_URL}/transactions/bnpl?plan=${selectedPlan.value.months}`
    }

    const res = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    })

    if (!res.ok) {
      const errText = await res.text()
      throw new Error(errText || 'Payment failed')
    }

    const saved = await res.json()

    // Save transaction ID if BNPL
    if (transaction.value.isBNPL || saved.isBNPL) {
      const txId = saved.id || saved.transactionId
      if (txId) {
        localStorage.setItem('lastBnplTransactionId', txId.toString())
      }
    }

    currentStep.value = 3
  } catch (err) {
    errorMessage.value = err.message || 'Payment failed'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// ─── Auto-load last BNPL Installments ────────────────────────
const goToInstallments = () => {
  const lastId = localStorage.getItem('lastBnplTransactionId')
  if (lastId) {
    searchTransactionId.value = lastId
  }
  currentStep.value = 4
  if (searchTransactionId.value) fetchInstallments()
}

// ─── Fetch Installments ──────────────────────────────────────
const fetchInstallments = async () => {
  if (!searchTransactionId.value?.trim()) {
    errorMessage.value = 'Please enter Transaction ID'
    return
  }

  loading.value = true
  displayedInstallments.value = []
  errorMessage.value = ''

  const token = getAuthToken()
  if (!token) {
    errorMessage.value = 'Please login again'
    loading.value = false
    return
  }

  try {
    const res = await fetch(
      `${API_BASE_URL}/bnpl/installments/transaction/${searchTransactionId.value}`,
      {
        headers: { Authorization: `Bearer ${token}` }
      }
    )

    if (!res.ok) {
      const err = await res.text()
      throw new Error(err || 'Failed to load installments')
    }

    const data = await res.json()
    displayedInstallments.value = Array.isArray(data) ? data : []
  } catch (err) {
    errorMessage.value = err.message || 'Could not load installments'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// ─── Fetch Transaction History ───────────────────────────────
const fetchTransactions = async () => {
  if (!searchCardId.value?.trim()) {
    errorMessage.value = 'Please enter Card ID (numeric)'
    return
  }

  loading.value = true
  transactions.value = []
  errorMessage.value = ''

  const token = getAuthToken()
  if (!token) {
    errorMessage.value = 'Please login again'
    loading.value = false
    return
  }

  try {
    const res = await fetch(
      `${API_BASE_URL}/transactions/card/${searchCardId.value}`,
      {
        headers: { Authorization: `Bearer ${token}` }
      }
    )

    if (!res.ok) {
      const err = await res.text()
      throw new Error(err || 'Failed to load transactions')
    }

    const data = await res.json()
    transactions.value = Array.isArray(data) ? data : []
  } catch (err) {
    errorMessage.value = err.message || 'Could not load transaction history'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const viewTransactionHistory = () => {
  currentStep.value = 5
  fetchTransactions()
}

const viewTransactionInstallments = (txId) => {
  searchTransactionId.value = txId.toString()
  currentStep.value = 4
  fetchInstallments()
}

const resetForm = () => {
  currentStep.value = 0
  transaction.value = {
    cardNumber: '',
    cvv: '',
    expiryMonth: '',
    expiryYear: '',
    amount: null,
    category: '',
    merchantName: '',
    isBNPL: false
  }
  eligibilityResult.value = { eligible: false, message: '' }
  selectedPaymentMethod.value = null
  selectedPlan.value = null
  searchTransactionId.value = ''
  displayedInstallments.value = []
  errorMessage.value = ''
  transactions.value = []
  searchCardId.value = ''
  // Keep lastBnplTransactionId for auto-load convenience
}

const selectPaymentMethod = (method) => {
  selectedPaymentMethod.value = method
  transaction.value.isBNPL = method === 'bnpl'
  if (method === 'full') selectedPlan.value = null
}

const selectInstallmentPlan = (plan) => {
  selectedPlan.value = plan
}

const proceedToConfirmation = () => {
  if (canProceed.value) currentStep.value = 2
}

const proceedToPayInFull = () => {
  selectPaymentMethod('full')
  currentStep.value = 2
}

const payInstallment = async (installment) => {
  // Implement actual payment call to /api/bnpl/installments/{id}/pay
  alert(`Paying installment ${installment.id} - amount: ${installment.amount}`)
  // After success → refresh list
  await fetchInstallments()
}

const formatDate = (date) => date ? new Date(date).toLocaleDateString('en-IN') : 'N/A'
const isOverdue = (dueDate) => dueDate && new Date(dueDate) < new Date()
const calculateTotalAmount = (items) => items.reduce((sum, i) => sum + Number(i.amount || 0), 0).toFixed(2)
const getStatusClass = (status) => {
  if (!status) return 'bg-gray-100 text-gray-700'
  if (status.toLowerCase().includes('paid') || status === 'Completed') return 'bg-green-100 text-green-700'
  if (status.toLowerCase().includes('pending')) return 'bg-yellow-100 text-yellow-700'
  if (status.toLowerCase().includes('overdue')) return 'bg-red-100 text-red-700'
  return 'bg-gray-100 text-gray-700'
}

const filterInstallments = (filter) => {
  installmentFilter.value = filter
  // Add filtering logic if needed
}

const filterTransactions = (filter) => {
  transactionFilter.value = filter
  // Add filtering logic if needed
}

onMounted(() => {
  // Optional: load last used card or transaction
})
</script>

<style>
/* Keep your existing :root and classes */
</style>