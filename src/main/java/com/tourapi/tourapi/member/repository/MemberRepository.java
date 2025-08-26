package com.tourapi.tourapi.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourapi.tourapi.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
}


