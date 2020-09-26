import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;

import java.util.List;

public class TestPut {

    public static void main(String[] args) {
        final String serverList = "127.0.0.1:8891," +
                "127.0.0.1:8892," +
                "127.0.0.1:8893," +
                "127.0.0.1:8894," +
                "127.0.0.1:8895";

        final RheaKVStore rheaKVStore = new DefaultRheaKVStore();

        final List<RegionRouteTableOptions> regionRouteTableOptions = MultiRegionRouteTableOptionsConfigured
                .newConfigured()
                .withInitialServerList(-1L, serverList)
                .config();

        final PlacementDriverOptions placementDriverOptions = PlacementDriverOptionsConfigured
                .newConfigured()
                .withFake(true)
                .withRegionRouteTableOptionsList(regionRouteTableOptions)
                .config();

        final RheaKVStoreOptions rheaKVStoreOptions = RheaKVStoreOptionsConfigured
                .newConfigured()
                .withInitialServerList(serverList)
                .withPlacementDriverOptions(placementDriverOptions)
                .config();

        rheaKVStore.init(rheaKVStoreOptions);

        String key = "test";
        String value = "hello world";

        rheaKVStore.bPut(key.getBytes(), value.getBytes());
    }

}
