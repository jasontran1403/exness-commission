package com.exnesscommission.service;

import com.exnesscommission.dto.MailDto;

import jakarta.mail.MessagingException;

public interface MailService {
	void send(MailDto mail) throws MessagingException;
}
