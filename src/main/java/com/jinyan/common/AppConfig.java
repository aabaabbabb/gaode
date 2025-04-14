package com.jinyan.common;


import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.json.MixedJsonFactory;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.render.JsonRender;
import com.jfinal.template.Engine;
import com.jinyan.controller.interceptor.AdminAuthInterceptor;
import com.jinyan.controller.interceptor.LoginSessionInterceptor;
import com.jinyan.kit.AdminAuthKit;
import com.jinyan.kit.SharedMethodLib;
import com.jinyan.kit.directive.PermissionDirective;
import com.jinyan.kit.directive.RoleDirective;
import com.jinyan.model._MappingKit;
import com.jinyan.service.LoginService;



/**
 * 配置中心
 */
public class AppConfig extends JFinalConfig {
	
	static Prop p;
	
	static void loadConfig() {
		if (p == null) {
			// 加载从左到右第一个被找到的配置文�?
			p = PropKit.useFirstFound("app-config-pro.txt", "app-config-dev.txt");
		}
	}
	
	public void configConstant(Constants me) {
		loadConfig();
		me.setDevMode(p.getBoolean("devMode", false));
		
		// 使用 JFinalJson �? json string。使�? FastJson �? java 对象
		me.setJsonFactory(MixedJsonFactory.me());
		
		// 使用 cglib 生成 aop 代理，�?�应更多场景
		me.setToCglibProxyFactory();
		
		// 支持 Controller、Interceptor、Validator 之中使用 @Inject 注入业务层，并且自动实现 AOP
		me.setInjectDependency(true);
		
		// 不对父类进行注入，提升注入�?�能
		me.setInjectSuperClass(false);
	}
	
	public void configRoute(Routes me) {
		// 添加后台路由
		me.add(new Routes() {
			public void config() {
				// 添加路由级别的拦截器，拦截所有在此方法中添加�? Controller 中的�?�? action
				this.addInterceptor(new AdminAuthInterceptor());
				this.addInterceptor(new LayoutInterceptor());
				
				// 配置视图的基�?路径，避�? render(...) 参数输入前缀 "/_view/admin"
				this.setBaseViewPath("/_view");
				
				// 扫描后台路由
				this.scan("com.jinyan.controller.");
			}
		});
		
		/* 添加前台路由 ---> 添加前台功能时开启下面的代码
		me.add(new Routes() {
			public void config() {
				// 配置视图的基�?路径
				this.setBaseViewPath("/_view");
				
				// 扫描前台路由，过滤掉后台路由的扫�?
				this.scan("com.jfinal.", className -> {
					// className 为当前正扫描的类名，返回 true 时表示过滤掉当前类不扫描
					return className.startsWith("com.jfinal.admin.");
				});				
			}
		}); */
	}
	
	public void configEngine(Engine me) {
		// devMode �? true 时支持模板文件热加载
		me.setDevMode(p.getBoolean("engineDevMode", false));
		
		// �?启压缩功�?
		// me.setCompressorOn();
		
		// 添加共享对象
		me.addSharedObject("StrKit", new StrKit());
		//me.addSharedObject("RunningTime", new RunningTime());
		
		// 添加角色、权�? shared method
		me.addSharedMethod(AdminAuthKit.class);
		me.addSharedMethod(new SharedMethodLib());				// 添加共享方法�?
		
		// 添加角色、权限指�?
		me.addDirective("role", RoleDirective.class);
		me.addDirective("permission", PermissionDirective.class);
		me.addDirective("perm", PermissionDirective.class);		// 添加�?个别名指�?
		
		// 添加后台分页模板函数
		me.addSharedFunction("/_view/admin/common/_paginate.html");
	}
	
	/**
	 * 抽取成独立的方法，便�? _Generator 中重用该方法，减少代码冗�?
	 */
	public static DruidPlugin getDruidPlugin() {
		loadConfig();
		return new DruidPlugin(p.get("jdbcUrl"), p.get("user"), p.get("password").trim());
	}
	
	public void configPlugin(Plugins me) {
		// 配置 JDBC 连接池插�?
		DruidPlugin druidPlugin = getDruidPlugin();
		WallFilter wallFilter = new WallFilter();			// 加强数据库安�?
		wallFilter.setDbType("mysql");
		druidPlugin.addFilter(wallFilter);
		me.add(druidPlugin);


		//配置缓存插件
		me.add(new EhCachePlugin());

		// 配置 ActiveRecordPlugin
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		arp.setShowSql(p.getBoolean("devMode", false));	// 是否输出 sql 到控制台
		arp.addSqlTemplate("/com/jinyan/common/sql/_all_sqls.sql");
		_MappingKit.mapping(arp);	// 自动添加 model �? table 的映�?
		me.add(arp);
		
		RedisPlugin hotelRedis = new RedisPlugin("hotel", "gd.tetuijiudian.cn",6380,"wfor8890");
		//RedisPlugin hotelRedis = new RedisPlugin("hotel", "127.0.0.1");
		System.out.println("hotelRedis:"+hotelRedis);
	    me.add(hotelRedis);
	}
	
	public void configInterceptor(Interceptors me) {
		// 登录会话拦截�?
		me.add(new LoginSessionInterceptor());
	}
	
	public void configHandler(Handlers me) {}
	
	// 服务启动时回�? onStart()
	public void onStart() {
		// 调用不带参的 renderJson() 时，排除对指定变量名�? json 转换
		JsonRender.addExcludedAttrs(LoginService.LOGIN_ACCOUNT);
	}
	
	// 服务关闭时回�? onStop()
	public void onStop() {
		
	}
}





