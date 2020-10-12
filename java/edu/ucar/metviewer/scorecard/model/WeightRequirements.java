package edu.ucar.metviewer.scorecard.model;

import java.util.List;

public class WeightRequirements {
  private final String name;
  private List<Weight> weightList;


  public WeightRequirements(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<Weight> getWeightList() {
    return weightList;
  }

  public void setWeightList(List<Weight> weightList) {
    this.weightList = weightList;
  }

  public static class Weight {
    private final double weight;
    private  String color = null;
    private final List<Criteria> criteriaList;

    public Weight(double weight, String color, List<Criteria> criteriaList) {
      this.weight = weight;
      this.color = color;
      this.criteriaList = criteriaList;
    }

    public double getWeight() {
      return weight;
    }

    public String getColor() {
      return color;
    }

    public List<Criteria> getCriteriaList() {
      return criteriaList;
    }


  }

  public static class Criteria {
    private final String field;
    private final String value;

    public Criteria(String field, String value) {
      this.field = field;
      this.value = value;
    }

    public String getField() {
      return field;
    }

    public String getValue() {
      return value;
    }
  }
}
