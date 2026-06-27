package com.study.authz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 폴더(자기참조 계층). {@code parentFolder} 가 null 이면 루트.
 * {@code ownerId}/{@code sensitivityLevel} 은 Stage 2(ABAC)/Stage 3(ReBAC 전이) 에서 활용한다.
 */
@Entity
@Table(name = "folders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private Folder parentFolder;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private int sensitivityLevel;

    public Folder(String name, Folder parentFolder, Long ownerId, int sensitivityLevel) {
        this.name = name;
        this.parentFolder = parentFolder;
        this.ownerId = ownerId;
        this.sensitivityLevel = sensitivityLevel;
    }
}
