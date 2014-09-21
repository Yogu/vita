package de.unistuttgart.vis.vita.model.entity;

import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Target;

/**
 * Represents a Relation between two Entities.
 *
 * @param <E> - the type of the other entity
 */
@javax.persistence.Entity
@NamedQueries({
    @NamedQuery(name = "EntityRelation.findAllEntityRelations", 
                query = "SELECT er "
                      + "FROM EntityRelation er"),
        
    @NamedQuery(name = "EntityRelation.findRelationsForEntities",
                query = "SELECT er "
                      + "FROM Entity e JOIN e.entityRelations er "
                      + "WHERE e.id IN :entityIds"),

    @NamedQuery(name = "EntityRelation.findEntityRelationById", 
                query = "SELECT er "
                      + "FROM EntityRelation er " 
                      + "WHERE er.id = :entityRelationId")})
public class EntityRelation<E> extends AbstractEntityBase {

  // constants
  private static final int WEIGHT_MIN = 0;
  private static final int WEIGHT_MAX = 1;

  private double weight;
  
  // only entity relations will be persisted
  @Target(Entity.class)
  @ManyToOne
  @JoinTable(name="OriginId")
  private E originEntity;
  
  // only entity relations will be persisted
  @Target(Entity.class)
  @ManyToOne
  @JoinTable(name="TargetId")
  private E relatedEntity;

  public E getOriginEntity() {
    return originEntity;
  }

  public void setOriginEntity(E originEntity) {
    this.originEntity = originEntity;
  }

  /**
   * @return entity which is target of this relation
   */
  public E getRelatedEntity() {
    return relatedEntity;
  }

  /**
   * Sets the entity which is target of this relation.
   *
   * @param relatedEntity - the related entity
   */
  public void setRelatedEntity(E relatedEntity) {
    this.relatedEntity = relatedEntity;
  }

  /**
   * Returns how strong this relation is, returning a value between 0 (very weak) and 1 (very
   * strong).
   *
   * @return the weight of this relation
   */
  public double getWeight() {
    return weight;
  }

  /**
   * Sets the weight of this relation, indicating how strong it is.
   *
   * @param weight - value from 0.0 (very weak relation) to 1.0 (very strong relation)
   */
  public void setWeight(double weight) {
    if (weight < WEIGHT_MIN || weight > WEIGHT_MAX) {
      throw new IllegalArgumentException("weight of relation must be between 0 and 1!");
    }
    this.weight = weight;
  }

}
