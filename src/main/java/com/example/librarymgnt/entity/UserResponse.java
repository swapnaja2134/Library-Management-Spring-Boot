package com.example.librarymgnt.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

	private Integer id;
	private String name;
	private String username;
	private String email;
	private Role role;
	private Boolean enabled;
	private String token;

	public UserResponse(Integer id, String name, String username, String email, Role role, Boolean enabled, String token) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.email = email;
		this.role = role;
		this.enabled = enabled;
		this.token = token;
	}

	public UserResponse() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
