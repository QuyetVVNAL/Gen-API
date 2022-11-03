package com.example.Test.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Column {

    String name;
    String size;
    String type;
    String isNullAble;
    String isAutoIncrement;
    String primaryKey;
    String foreignKey;
}
