import java.net.URL;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.setvect.bokslphoto.BokslPhotoApplication;
import com.setvect.bokslphoto.EnvirmentProperty;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private PhotoService photoService;
		
	photoService.retrievalPhoto();

	FolderVo folder = new FolderVo();
	folder.setFolderSeq(0);
	folder.setParentId(0);
	folder.setName("ROOT");
	folderRepository.save(folder);

	UserVo user = new UserVo();
	user.setUserId("admin");
	user.setName("1234");
	user.setEmail("a@abcde.com");

	user.setDeleteF(false);

	PasswordEncoder encoder = new BCryptPasswordEncoder();
	user.setPassword(encoder.encode("1234"));
	Set<UserRoleVo> userRole = new HashSet<>();
	UserRoleVo role = new UserRoleVo();
	role.setRole("ROLE_ADMIN");
	role.setUser(user);
	userRole.add(role);
	user.setUserRole(userRole);

	role = new UserRoleVo();
	role.setRole("ROLE_USER");
	role.setUser(user);
	userRole.add(role);
	user.setUserRole(userRole);

	userRepository.save(user);
	
	
	
	
	===========================
			
			
			
	public class BokslPhotoApplication extends SpringBootServletInitializer {
		private static final String CONFIG_CONFIG_PROPERTIES = "/application.properties";

		@Override
		protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
			return application.sources(BokslPhotoApplication.class);
		}

		@Bean
		InitializingBean init() {
			return () -> {
				URL configUrl = BokslPhotoApplication.class.getResource(CONFIG_CONFIG_PROPERTIES);
				EnvirmentProperty.init(configUrl);
			};
		}
	}
			