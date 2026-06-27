package com.study.authz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문서. RBAC/ABAC/ReBAC 모든 단계가 인가 대상으로 공유하는 핵심 리소스.
 */
@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private int sensitivityLevel;

    public Document(String title, String content, Folder folder, Long ownerId, int sensitivityLevel) {
        this.title = title;
        this.content = content;
        this.folder = folder;
        this.ownerId = ownerId;
        this.sensitivityLevel = sensitivityLevel;
    }

    /** 본문 수정(EDITOR 이상의 write 액션에서 사용). */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
