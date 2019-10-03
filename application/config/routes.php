<?php
defined('BASEPATH') OR exit('No direct script access allowed');

/*
| -------------------------------------------------------------------------
| URI ROUTING
| -------------------------------------------------------------------------
| This file lets you re-map URI requests to specific controller functions.
|
| Typically there is a one-to-one relationship between a URL string
| and its corresponding controller class/method. The segments in a
| URL normally follow this pattern:
|
|	example.com/class/method/id/
|
| In some instances, however, you may want to remap this relationship
| so that a different class/function is called than the one
| corresponding to the URL.
|
| Please see the user guide for complete details:
|
|	https://codeigniter.com/user_guide/general/routing.html
|
| -------------------------------------------------------------------------
| RESERVED ROUTES
| -------------------------------------------------------------------------
|
| There are three reserved routes:
|
|	$route['default_controller'] = 'welcome';
|
| This route indicates which controller class should be loaded if the
| URI contains no data. In the above example, the "welcome" class
| would be loaded.
|
|	$route['404_override'] = 'errors/page_missing';
|
| This route will tell the Router which controller/method to use if those
| provided in the URL cannot be matched to a valid route.
|
|	$route['translate_uri_dashes'] = FALSE;
|
| This is not exactly a route, but allows you to automatically route
| controller and method names that contain dashes. '-' isn't a valid
| class or method name character, so it requires translation.
| When you set this option to TRUE, it will replace ALL dashes in the
| controller and method URI segments.
|
| Examples:	my-controller/index	-> my_controller/index
|		my-controller/my-method	-> my_controller/my_method
*/

$route['default_controller'] = 'app';
$route['teacher/signup'] = 'app/teacher_signup';
$route['teacher/signin'] = 'app/teacher_signin';
$route['teacher_dashboard'] = 'app/teacher_dashboard';
$route['teacher_logout'] = 'app/teacher_logout';
$route['add_class'] = 'app/add_class';
$route['add_item'] = 'app/add_item';
$route['items'] = 'app/all_items';
$route['items/(:any)'] = 'app/all_items/$1';
$route['all_student'] = 'app/all_student';
$route['teacher/edit_profile'] = 'app/teacher_edit_profile';
$route['teacher_change_password'] = 'app/teacher_change_password';

$route['student/signup'] = 'app/student_signup';
$route['student/signin'] = 'app/student_signin';
$route['student_dashboard'] = 'app/student_dashboard';
$route['student_logout'] = 'app/student_logout';
$route['item_details/(:any)/(:num)'] = 'app/item_details/$1/$2';
$route['student/edit_profile'] = 'app/student_edit_profile';
$route['student_change_password'] = 'app/student_change_password';

$route['edit_class/(:any)'] = 'app/edit_class/$1';
$route['download_file/(:any)'] = 'app/download/$1';
$route['about_us'] = 'app/about';

//Routes for admin

$route['translate_uri_dashes'] = FALSE;
