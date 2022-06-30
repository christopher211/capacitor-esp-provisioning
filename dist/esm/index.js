// export * from './definitions';
// export * from './web';
import { registerPlugin } from '@capacitor/core';
const EspProvisioning = registerPlugin('EspProvisioning', {
    web: () => import('./web').then(m => new m.EspProvisioningWeb()),
});
export * from './definitions';
export { EspProvisioning };
//# sourceMappingURL=index.js.map