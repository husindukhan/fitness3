package com.example.fitness;

import java.io.Serializable;

public class workout implements Serializable {

    private String id;
    private String name;
    private String type;
    private String description;
    private String muscleGroup;
    private String imageUrl;

    public workout() {
    }
         public workout(String id, String name, String type, String description, String muscleGroup, String imageUrl)
        {
            this.id = id;
            this.name = name;
            this.type = type;
            this.description = description;
            this.muscleGroup = muscleGroup;
            this.imageUrl = imageUrl;

        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getMuscleGroup() {
            return muscleGroup;
        }
        public void setMuscleGroup(String muscleGroup) {
            this.muscleGroup = muscleGroup;
        }
        public String getImageUrl() {
            return imageUrl;
        }
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
