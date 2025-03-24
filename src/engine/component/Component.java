package engine.component;

import engine.entity.Entity;

public abstract class Component {
	protected Entity entity;

	public Component(Entity entity) {
		this.entity = entity;
	}

    public Entity getParentEntity() {
        return entity;
    }
}
