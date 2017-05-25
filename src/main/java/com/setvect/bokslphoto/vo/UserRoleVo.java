package com.setvect.bokslphoto.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;

/**
 * 권한
 */
@Entity
@Table(name = "TBAB_ROLE")
public class UserRoleVo {
	/** 일련번호 */
	@Id
	@Column(name = "ROLE_SEQ")
	@GenericGenerator(name = "hibernate-increment", strategy = "increment")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "hibernate-increment")
	private int roleSeq;

	/** 사용자 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "username", nullable = false)
	private UserVo user;

	/** 권한 이름 */
	@Column(name = "ROLE", nullable = false, length = 20)
	private String role;

	/**
	 * @return 일련번호
	 */
	public int getRoleSeq() {
		return roleSeq;
	}

	/**
	 * @param userRoleId
	 *            일련번호
	 */
	public void setRoleSeq(final int userRoleId) {
		this.roleSeq = userRoleId;
	}

	/**
	 * @return 사용자
	 */
	public UserVo getUser() {
		return user;
	}

	/**
	 * @param user
	 *            사용자
	 */
	public void setUser(final UserVo user) {
		this.user = user;
	}

	/**
	 * @return 권한 이름
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role
	 *            권한 이름
	 */
	public void setRole(final String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		// 아랫 처럼 코딩 하면 @ManyToOne 레퍼런스 때문에 무한루프 빠짐
		// ToStringBuilder.reflectionToString(this);
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}