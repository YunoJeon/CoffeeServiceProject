package com.coffee.coffeeserviceproject.member.entity;

import com.coffee.coffeeserviceproject.member.dto.RoasterDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Roaster {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "member_id", nullable = false, unique = true)
  private Member member;

  private String roasterName;

  private String officeAddress;

  private String contactInfo;

  private String description;

  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public static Roaster fromEntity(Member member, RoasterDto roasterDto) {

    return Roaster.builder()
        .roasterName(roasterDto.getRoasterName())
        .officeAddress(roasterDto.getOfficeAddress())
        .contactInfo(
            roasterDto.getContactInfo() == null || roasterDto.getContactInfo().isEmpty()
                ? member.getPhone() : roasterDto.getContactInfo())
        .description(roasterDto.getDescription())
        .member(member)
        .build();
  }
}