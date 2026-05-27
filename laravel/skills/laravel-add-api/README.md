# Laravel Add API Skill

Complete workflow for adding fully functional REST API endpoints to your Laravel application.

## What's Included

### Main Documentation
- **SKILL.md** - Complete step-by-step guide (13 phases)

### Templates (Ready-to-use code templates)
- `migration.template.php` - Database migration structure
- `model.template.php` - Eloquent model with relationships
- `factory.template.php` - Test data factory
- `controller.template.php` - API controller with CRUD methods
- `policy.template.php` - Authorization policy
- `seeder.template.php` - Database seeder
- `test.template.php` - Feature test suite

### Reference Documentation
- `relationships.md` - Model relationship patterns
- `api-responses.md` - Response format standards
- `policies.md` - Authorization & access control
- `testing.md` - Testing best practices

## Quick Start

### For Adding a New API Resource:

1. **Read the SKILL.md** - Understand the 13-phase workflow
2. **Run the artisan command** - Generate model and scaffolding
   ```bash
   php artisan make:model Review -mfsc --api
   ```
3. **Follow each phase** - Database, Model, Factory, Controller, etc.
4. **Use templates** - Copy from template files as reference
5. **Check references** - Use docs for details on relationships, responses, policies

## Time Estimate

- **First time:** 1.5 - 2.5 hours (learning + implementation)
- **Subsequent times:** 30 - 45 minutes (you'll memorize the process)

## Phases Overview

1. **Plan & Requirements** (5-10 min)
2. **Generate Model & Migration** (2-3 min)
3. **Define Database Schema** (10-15 min)
4. **Define Model & Relationships** (10-15 min)
5. **Create Factory** (5-10 min)
6. **Define Controller** (15-20 min)
7. **Create Policy** (5-10 min)
8. **Register Routes** (5 min)
9. **Create Seeder** (5-10 min)
10. **Run Migrations & Seeders** (2 min)
11. **Create Tests** (15-20 min)
12. **Run Tests & Verify** (5-10 min)
13. **Manual API Testing** (10-15 min)

## File Structure

```
.github/skills/laravel-add-api/
├── SKILL.md                          # Main documentation
├── README.md                         # This file
├── templates/
│   ├── migration.template.php        # Database migration
│   ├── model.template.php            # Eloquent model
│   ├── factory.template.php          # Test factory
│   ├── controller.template.php       # API controller
│   ├── policy.template.php           # Authorization policy
│   ├── seeder.template.php           # Database seeder
│   └── test.template.php             # Feature tests
└── references/
    ├── relationships.md              # Model relationships
    ├── api-responses.md              # Response format
    ├── policies.md                   # Authorization
    └── testing.md                    # Testing guide
```

## Key Artifacts Generated

For a resource named "Review", you'll get:

- ✅ `app/Models/Review.php` - Eloquent model
- ✅ `database/migrations/YYYY_MM_DD_*_create_reviews_table.php` - Database table
- ✅ `database/factories/ReviewFactory.php` - Test data factory
- ✅ `database/seeders/ReviewSeeder.php` - Database seeder
- ✅ `app/Http/Controllers/ReviewController.php` - API controller (5 CRUD methods)
- ✅ `app/Policies/ReviewPolicy.php` - Authorization rules
- ✅ `tests/Feature/ReviewTest.php` - Complete test suite
- ✅ `routes/api.php` - API routes registered

## Checklist

Use this checklist to ensure complete implementation:

### Planning Phase
- [ ] Resource name decided
- [ ] Fields and columns documented
- [ ] Relationships identified
- [ ] API operations defined

### Scaffolding
- [ ] Model generated with -mfsc --api
- [ ] All files created

### Database
- [ ] Migration fields added
- [ ] Foreign keys defined
- [ ] Indexes added
- [ ] Migration executed

### Model
- [ ] $fillable populated
- [ ] $casts defined
- [ ] Relationships added
- [ ] HasApiTokens trait included

### Factory
- [ ] All fields have faker data
- [ ] Foreign keys use factory()
- [ ] State methods added

### Controller
- [ ] All 5 CRUD methods implemented
- [ ] Validation added
- [ ] Eager loading in queries
- [ ] Proper HTTP responses

### Policy
- [ ] All 7 methods implemented
- [ ] Authorization logic correct

### Routes
- [ ] apiResource registered
- [ ] Auth middleware applied
- [ ] Correct URL prefix

### Seeder
- [ ] Seeder created
- [ ] Registered in DatabaseSeeder
- [ ] Tested

### Tests
- [ ] All CRUD tests passing
- [ ] Authorization tests
- [ ] Validation tests
- [ ] Database assertions

### Verification
- [ ] Manual API testing complete
- [ ] All endpoints working
- [ ] Relationships loading correctly

## Example Usage

```bash
# 1. Decide on resource: Comment
# 2. Generate scaffolding
php artisan make:model Comment -mfsc --api

# 3. Edit migration (database/migrations/...)
# Add fields: user_id, post_id, body, etc.

# 4. Edit model (app/Models/Comment.php)
# Add relationships to User and Post

# 5. Edit factory (database/factories/CommentFactory.php)
# Add faker data

# 6. Edit controller (app/Http/Controllers/CommentController.php)
# Implement CRUD methods

# 7. Edit policy (app/Policies/CommentPolicy.php)
# Add authorization rules

# 8. Register routes in routes/api.php
# Route::apiResource('comments', CommentController::class);

# 9. Edit seeder (database/seeders/CommentSeeder.php)

# 10. Run database
php artisan migrate
php artisan db:seed --class=CommentSeeder

# 11. Create tests (tests/Feature/CommentTest.php)

# 12. Run tests
php artisan test tests/Feature/CommentTest.php

# 13. Manual testing
php artisan serve
# Test with curl or Postman
```

## Reference Quick Links

- **Relationships:** See `references/relationships.md`
- **API Responses:** See `references/api-responses.md`
- **Authorization:** See `references/policies.md`
- **Testing:** See `references/testing.md`

## Common Issues

### Migration fails
- Check foreign key column names match
- Ensure related tables exist
- Run migrations in order

### Tests fail
- Use RefreshDatabase trait
- Create test data in setUp()
- Check assertions match response structure

### Routes not working
- Verify apiResource in routes/api.php
- Check middleware chain
- Confirm controller namespace

### Authorization failing
- Register policy in AuthServiceProvider
- Check policy methods have correct logic
- Verify authorizeResource() in controller constructor

## Further Customization

After following this skill, you can extend with:

- Custom API resources for formatting
- Event listeners for auditing
- Query filters and scopes
- Soft deletes for archival
- API versioning
- Rate limiting
- Caching strategies

## Next Steps

1. **Read SKILL.md** thoroughly
2. **Decide on first resource** to implement
3. **Run artisan commands** for scaffolding
4. **Follow each phase** in order
5. **Use templates** as reference
6. **Consult references** for details
7. **Test thoroughly** before deployment

---

**Happy API building!** 🚀

For questions, refer to the [Laravel Documentation](https://laravel.com/docs) or the reference files included.
