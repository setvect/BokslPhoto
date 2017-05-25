package com.setvect.bokslphoto.vo;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Type;

/**
 * 사용자
 */
@Entity
@Table(name = "TBAA_USER")
public class UserVo {
	/** 사용자 아이디 */
	@Id
	@Column(name = "USER_ID", unique = true, nullable = false, length = 20)
	private String userId;

	/** 이름 */
	@Column(name = "NAME", nullable = false, length = 50)
	private String name;

	/** 비밀번호 */
	@Column(name = "PASSWD", nullable = false, length = 60)
	private String password;

	/** 이메일 */
	@Column(name = "EMAIL", nullable = false, length = 100)
	private String email;

	/** 삭제 여부 */
	@Column(name = "DELETE_F", nullable = false, length = 1)
	@Type(type = "yes_no")
	private boolean deleteF;

	/** 보유 권한 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
	private Set<UserRoleVo> userRole;

	/**
	 * @return 사용자 아이디
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            사용자 아이디
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	/**
	 * @return 이름
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            이름
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return 비밀번호
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            비밀번호
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * @return 이메일
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            이메일
	 */
	public void setEmail(final String email) {
		this.email = email;
	}

	/**
	 * @return 삭제 여부
	 */
	public boolean isDeleteF() {
		return deleteF;
	}

	/**
	 * @param deleteF
	 *            삭제 여부
	 */
	public void setDeleteF(final boolean deleteF) {
		this.deleteF = deleteF;
	}

	/**
	 * @return 보유 권한
	 */
	public Set<UserRoleVo> getUserRole() {
		return userRole;
	}

	/**
	 * @param userRole
	 *            보유 권한
	 */
	public void setUserRole(final Set<UserRoleVo> userRole) {
		this.userRole = userRole;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}