package demo2;

import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.solon.integration.SaTokenRouterInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

/**
 * @author noear 2022/7/11 created
 */
@Configuration
public class Config {
    @Bean
    public void saTokenPathInterceptor() {
        Solon.app().before(new SaTokenRouterInterceptor((req, res, e) -> {
            SaRouter.match("/**", StpUtil::checkLogin);
            // 根据路由划分模块，不同模块不同鉴权
            SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
            SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
            SaRouter.match("/goods/**", r -> StpUtil.checkPermission("goods"));
            SaRouter.match("/orders/**", r -> StpUtil.checkPermission("orders"));
        }));
    }

    @Bean
    public void saTokenPathInterceptor2() {
        Solon.app().before((ctx) -> {
            SaRouter.match("/**", StpUtil::checkLogin);
            // 根据路由划分模块，不同模块不同鉴权
            SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
            SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
            SaRouter.match("/goods/**", r -> StpUtil.checkPermission("goods"));
            SaRouter.match("/orders/**", r -> StpUtil.checkPermission("orders"));
        });
    }
}
