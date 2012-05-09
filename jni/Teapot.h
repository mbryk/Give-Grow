/*==============================================================================
            Copyright (c) 2012 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    Teapot.h

@brief
    Geometry for the teapot used in the samples.

==============================================================================*/

#ifndef _QCAR_TEAPOT_OBJECT_H_
#define _QCAR_TEAPOT_OBJECT_H_


#define NUM_TEAPOT_OBJECT_VERTEX 824
#define NUM_TEAPOT_OBJECT_INDEX 1024 * 3


static const float teapotVertices[NUM_TEAPOT_OBJECT_VERTEX * 3] =
{
    // 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0,
    
    -0.5, -0.5, 0.0, 0.5, -0.5, 0.0, 0.5, 0.5, 0.0, -0.5, 0.5, 0.0,
};

static const float teapotTexCoords[NUM_TEAPOT_OBJECT_VERTEX * 2] =
{
    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0
};

static const float teapotNormals[NUM_TEAPOT_OBJECT_VERTEX * 3] =
{
    0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0
};

static const unsigned short teapotIndices[NUM_TEAPOT_OBJECT_INDEX] =
{
    0, 1, 2, 0, 2, 3
};


#endif // _QCAR_TEAPOT_OBJECT_H_
