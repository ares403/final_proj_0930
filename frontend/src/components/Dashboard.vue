<template>

<v-data-table
    :headers="headers"
    :items="dashboard"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'Dashboard',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "customerId", value: "customerId" },
            { text: "reserveId", value: "reserveId" },
            { text: "vaccineId", value: "vaccineId" },
            { text: "customerName", value: "customerName" },
            { text: "vaccineName", value: "vaccineName" },
            { text: "reserveStatus", value: "reserveStatus" },
        ],
        dashboard : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/dashboards')

      this.dashboard = temp.data._embedded.dashboards;

    },
    methods: {
    }
  }
</script>

