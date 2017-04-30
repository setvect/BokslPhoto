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