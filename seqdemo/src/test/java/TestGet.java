import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class TestGet {

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
                //todo: 是否需要下面这一句？
                .withInitialServerList(serverList)
                .withPlacementDriverOptions(placementDriverOptions)
                .config();

        rheaKVStore.init(rheaKVStoreOptions);

        String key = "test";

        byte[] valueBytes = rheaKVStore.bGet(key.getBytes());
        log.info("Value = {}", new String(valueBytes));
    }

}
