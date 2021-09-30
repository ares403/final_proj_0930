
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import CustomerManager from "./components/CustomerManager"


import Dashboard from "./components/Dashboard"
import ReservationManager from "./components/ReservationManager"

import VaccineManager from "./components/VaccineManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/customers',
                name: 'CustomerManager',
                component: CustomerManager
            },


            {
                path: '/dashboards',
                name: 'Dashboard',
                component: Dashboard
            },
            {
                path: '/reservations',
                name: 'ReservationManager',
                component: ReservationManager
            },

            {
                path: '/vaccines',
                name: 'VaccineManager',
                component: VaccineManager
            },



    ]
})
