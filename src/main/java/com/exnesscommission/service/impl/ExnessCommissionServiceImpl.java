package com.exnesscommission.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exnesscommission.entity.ExnessCommission;
import com.exnesscommission.repository.ExnessCommissionRepository;
import com.exnesscommission.service.ExnessCommissionService;

@Service
public class ExnessCommissionServiceImpl implements ExnessCommissionService{
	@Autowired
	ExnessCommissionRepository exCommissionRepo;

	@Override
	public void saveListExnessCommission(List<ExnessCommission> request) {
		// TODO Auto-generated method stub
		exCommissionRepo.saveAll(request);
	}

}
