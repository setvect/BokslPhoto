package com.setvect.bokslphoto;

import javax.transaction.Transactional;

import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BokslPhotoApplication.class })
@Transactional
@Rollback(true)
public class MainTestBase {

}
