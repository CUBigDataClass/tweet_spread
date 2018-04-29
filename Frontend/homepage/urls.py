from django.conf.urls import url, include
from django.contrib import admin
from django.conf.urls import url
from . import views

urlpatterns = [

	url(r'^$', views.index, name='index'),
	url(r'^search', views.home, name='home'),
	url(r'^search', views.update_view, name='update_view'),
]
