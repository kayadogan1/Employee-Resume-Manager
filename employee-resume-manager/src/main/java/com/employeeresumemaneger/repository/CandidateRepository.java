package com.employeeresumemaneger.repository;

import com.employeeresumemaneger.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
}
