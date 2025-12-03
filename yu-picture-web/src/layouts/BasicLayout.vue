<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>

      <a-layout-footer class="footer">
        <a href="https://com.github.ecc1esia" target="_blank"> ecc1esia </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
</script>

<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  background: white;
  color: unset;
  margin-bottom: 10px;
}

#basicLayout .sider {
  background: #fff;
  border-right: 0.5px solid #eee;
  padding-top: 20px;
}

#basicLayout .content {
  padding: 28px;
  background: linear-gradient(to right, #fefefe, #fff);
  margin-bottom: 28px;
}

#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
