from sqlalchemy.orm import Session
import models
import auth


def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()

def create_user(db: Session, email: str, password: str):
    hashed_pw = auth.get_password_hash(password)
    user = models.User(email=email, hashed_password=hashed_pw)
    db.add(user)
    db.commit()
    db.refresh(user)
    return user

def get_topics(db: Session):
    return db.query(models.Topic).all()

def get_topic(db: Session, topic_id: int):
    return db.query(models.Topic).filter(models.Topic.id == topic_id).first()

def create_progress(db: Session, user_id: int, topic_id: int, status: str):
    progress = models.Progress(user_id=user_id, topic_id=topic_id, status=status)
    db.add(progress)
    db.commit()
    db.refresh(progress)
    return progress

def get_progress(db: Session, user_id: int):
    return db.query(models.Progress).filter(models.Progress.user_id == user_id).all()
