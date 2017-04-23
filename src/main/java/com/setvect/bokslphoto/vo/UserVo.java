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

@Entity
@Table(name = "TBAA_USER")
public class UserVo {
	@Id
	@Column(name = "USER_ID", unique = true, nullable = false, length = 20)
	private String userId;

	@Column(name = "NAME", nullable = false, length = 50)
	private String name;

	@Column(name = "PASSWD", nullable = false, length = 60)
	private String password;

	@Column(name = "EMAIL", nullable = false, length = 100)
	private String email;

	@Column(name = "DELETE_F", nullable = false, length = 1)
	@Type(type = "yes_no")
	private boolean deleteF;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
	private Set<UserRoleVo> userRole;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isDeleteF() {
		return deleteF;
	}

	public void setDeleteF(boolean deleteF) {
		this.deleteF = deleteF;
	}

	public Set<UserRoleVo> getUserRole() {
		return userRole;
	}

	public void setUserRole(Set<UserRoleVo> userRole) {
		this.userRole = userRole;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}