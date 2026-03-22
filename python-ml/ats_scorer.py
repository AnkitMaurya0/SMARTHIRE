import sys
import json
import re


def clean_text(text):
    text = text.lower()
    text = re.sub(r'\s+', ' ', text)
    return text.strip()


def calculate_skills_score(resume_text, skills_list):
    """
    Skills matching - 40% weight
    Checks each required skill in resume
    """
    if not skills_list:
        return 0.0

    resume_clean = clean_text(resume_text)
    matched = 0

    for skill in skills_list:
        if clean_text(skill) in resume_clean:
            matched += 1

    return round((matched / len(skills_list)) * 100, 2)


def calculate_education_score(resume_text, job_skills):
    """
    Education matching - 20% weight
    Checks education keywords in resume
    """
    resume_clean = clean_text(resume_text)

    # Common education keywords
    education_keywords = [
        'b.tech', 'btech', 'bachelor', 'b.e', 'be',
        'mca', 'bca', 'm.tech', 'mtech', 'master',
        'phd', 'diploma', 'graduation', 'graduate',
        'computer science', 'information technology',
        'software engineering', 'electronics'
    ]

    matched = 0
    for keyword in education_keywords:
        if keyword in resume_clean:
            matched += 1

    score = min((matched / 3) * 100, 100)
    return round(score, 2)


def calculate_experience_score(resume_text):
    """
    Experience detection - 15% weight
    Checks experience related keywords
    """
    resume_clean = clean_text(resume_text)

    experience_keywords = [
        'experience', 'worked', 'internship', 'project',
        'developed', 'built', 'designed', 'implemented',
        'created', 'managed', 'led', 'years', 'months',
        'company', 'organization', 'team', 'deployed'
    ]

    matched = 0
    for keyword in experience_keywords:
        if keyword in resume_clean:
            matched += 1

    score = min((matched / 5) * 100, 100)
    return round(score, 2)


def calculate_keyword_score(resume_text, job_skills):
    """
    General keyword matching - 15% weight
    Checks technical and professional keywords
    """
    resume_clean = clean_text(resume_text)
    skill_words = set(re.findall(r'\b\w+\b', job_skills.lower()))
    resume_words = set(re.findall(r'\b\w+\b', resume_clean))

    # Remove common stop words
    stop_words = {'and', 'or', 'the', 'a', 'an', 'in',
                  'of', 'to', 'for', 'with', 'is', 'are'}
    skill_words -= stop_words
    resume_words -= stop_words

    if not skill_words:
        return 0.0

    common = skill_words & resume_words
    score = (len(common) / len(skill_words)) * 100
    return round(score, 2)


def calculate_resume_quality_score(resume_text):
    """
    Resume quality check - 10% weight
    Checks if resume has important sections
    """
    resume_clean = clean_text(resume_text)

    quality_sections = [
        'email', 'phone', 'contact',
        'education', 'skills', 'project',
        'experience', 'objective', 'summary',
        'certificate', 'achievement', 'linkedin'
    ]

    matched = 0
    for section in quality_sections:
        if section in resume_clean:
            matched += 1

    score = min((matched / 6) * 100, 100)
    return round(score, 2)


def get_matched_skills(resume_text, skills_list):
    resume_clean = clean_text(resume_text)
    return [s.strip() for s in skills_list
            if clean_text(s) in resume_clean]


def get_missing_skills(resume_text, skills_list):
    resume_clean = clean_text(resume_text)
    return [s.strip() for s in skills_list
            if clean_text(s) not in resume_clean]


def get_score_label(score):
    if score >= 75:
        return "Excellent Match"
    elif score >= 55:
        return "Good Match"
    elif score >= 35:
        return "Average Match"
    else:
        return "Low Match"


def calculate_ats_score(resume_text, required_skills):
    skills_list = [s.strip() for s in required_skills.split(',')
                   if s.strip()]

    if not skills_list:
        return {
            "score": 0.0,
            "skills_score": 0.0,
            "education_score": 0.0,
            "experience_score": 0.0,
            "keyword_score": 0.0,
            "quality_score": 0.0,
            "matched": [],
            "missing": [],
            "label": "Low Match"
        }

    # Calculate each component
    skills_score     = calculate_skills_score(resume_text, skills_list)
    education_score  = calculate_education_score(resume_text, required_skills)
    experience_score = calculate_experience_score(resume_text)
    keyword_score    = calculate_keyword_score(resume_text, required_skills)
    quality_score    = calculate_resume_quality_score(resume_text)

    # Final score with weights
    final_score = (
        (skills_score     * 0.40) +
        (education_score  * 0.20) +
        (experience_score * 0.15) +
        (keyword_score    * 0.15) +
        (quality_score    * 0.10)
    )
    final_score = round(final_score, 2)

    matched = get_matched_skills(resume_text, skills_list)
    missing = get_missing_skills(resume_text, skills_list)
    label   = get_score_label(final_score)

    return {
        "score":            final_score,
        "skills_score":     skills_score,
        "education_score":  education_score,
        "experience_score": experience_score,
        "keyword_score":    keyword_score,
        "quality_score":    quality_score,
        "matched":          matched,
        "missing":          missing,
        "label":            label
    }


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(json.dumps({
            "score": 0.0, "matched": [], "missing": [],
            "label": "Low Match",
            "error": "Missing arguments"
        }))
        sys.exit(1)

    resume_text     = sys.argv[1]
    required_skills = sys.argv[2]

    result = calculate_ats_score(resume_text, required_skills)
    print(json.dumps(result))