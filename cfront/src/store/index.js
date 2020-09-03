import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    balanceData: 0,
    posiData: [],
    orderData: [],
    tradeData: [],
  },
  mutations: {
    updateBalance(state, balanceData) {
      state.balanceData = balanceData;
    },
    updatePosi(state, posiData) {
      state.posiData = posiData;
    },
    updateOrder(state, orderData) {
      state.orderData = orderData;
    },
    updateTrade(state, tradeData) {
      state.tradeData = tradeData;
    },
  },
  actions: {
  },
  modules: {
  }
})
