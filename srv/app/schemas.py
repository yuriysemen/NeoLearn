from pydantic import BaseModel, EmailStr
from typing import List, Optional

class UserCreate(BaseModel):
    email: EmailStr
    password: str

class UserOut(BaseModel):
    id: int
    email: EmailStr

    class Config:
        orm_mode = True

class Token(BaseModel):
    access_token: str
    token_type: str

class TopicBase(BaseModel):
    id: int
    title: str

    class Config:
        orm_mode = True

class TopicDetail(TopicBase):
    html_content: str

class ProgressCreate(BaseModel):
    topic_id: int
    status: str

class ProgressOut(BaseModel):
    topic_id: int
    status: str
    updated_at: str

    class Config:
        orm_mode = True
