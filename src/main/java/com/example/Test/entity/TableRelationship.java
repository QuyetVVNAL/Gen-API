package com.example.Test.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TableRelationship {
    String pkTableName;
    String pkColumnName;
    String fkTableName;
    String fkColumnName;
    String relation;
}
